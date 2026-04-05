package com.example.aipet.network;

import androidx.annotation.Nullable;

import com.example.aipet.network.request.ApiRequestFactory;
import com.example.aipet.network.request.NetworkRequestExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;

/**
 * 聊天请求路由器（分布式功能）
 *
 * 负责根据 Provider 分发到不同的请求实现，
 * 让 ApiClient 专注于总引导和配置管理。
 */
public final class NetworkChatRouter {

    private NetworkChatRouter() {
    }

    public static void routeChatMessage(ApiClient.ApiService apiService,
                                        ApiConfig apiConfig,
                                        String normalizedContent,
                                        long petId,
                                        @Nullable ChatRequest.PetInfo petInfo,
                                        @Nullable String systemPrompt,
                                        @Nullable ApiClient.ChatCallback callback,
                                        Consumer<String> logger) {
        ApiConfig.ApiProvider provider = apiConfig.getProvider();
        switch (provider) {
            case OPENAI:
                sendOpenAI(apiService, apiConfig, normalizedContent, systemPrompt, callback, logger);
                break;
            case DOUBAO:
                sendDoubao(apiConfig, normalizedContent, systemPrompt, callback, logger);
                break;
            case LOCAL_BACKEND:
            case CUSTOM:
            default:
                sendCustom(apiService, normalizedContent, petId, petInfo, systemPrompt, callback, logger);
                break;
        }
    }

    public static void routeFullResponse(ApiClient.ApiService apiService,
                                         ApiConfig apiConfig,
                                         String normalizedContent,
                                         long petId,
                                         @Nullable ChatRequest.PetInfo petInfo,
                                         @Nullable List<ChatRequest.Message> historyMessages,
                                         @Nullable ApiClient.ChatResponseCallback callback,
                                         Consumer<String> logger) {
        ApiConfig.ApiProvider provider = apiConfig.getProvider();
        switch (provider) {
            case OPENAI:
                sendOpenAIFullResponse(apiService, apiConfig, normalizedContent, historyMessages, callback, logger);
                break;
            case DOUBAO:
                sendDoubaoFullResponse(apiConfig, normalizedContent, callback, logger);
                break;
            case LOCAL_BACKEND:
            case CUSTOM:
            default:
                sendCustomFullResponse(apiService, normalizedContent, petId, petInfo, historyMessages, callback, logger);
                break;
        }
    }

    private static void sendCustom(ApiClient.ApiService apiService,
                                   String normalizedContent,
                                   long petId,
                                   @Nullable ChatRequest.PetInfo petInfo,
                                   @Nullable String systemPrompt,
                                   @Nullable ApiClient.ChatCallback callback,
                                   Consumer<String> logger) {
        ChatRequest request = (ChatRequest) ApiRequestFactory.createChatRequest(normalizedContent, petId, petInfo);
        request.systemPrompt = systemPrompt;

        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            logger.accept("发送自定义格式消息");
        } else {
            logger.accept("发送自定义格式消息（含系统提示）");
        }

        executeChatCall(apiService.sendMessage(request), callback, "自定义格式", logger);
    }

    private static void sendOpenAI(ApiClient.ApiService apiService,
                                   ApiConfig apiConfig,
                                   String normalizedContent,
                                   @Nullable String systemPrompt,
                                   @Nullable ApiClient.ChatCallback callback,
                                   Consumer<String> logger) {
        String model = apiConfig.getApiModel();
        OpenAIChatRequest request = (OpenAIChatRequest) ApiRequestFactory.createOpenAIRequest(
                model,
                systemPrompt,
                normalizedContent,
                null
        );

        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            logger.accept("发送 OpenAI 格式消息 - 模型: " + model);
        } else {
            logger.accept("发送 OpenAI 格式消息（含系统提示）");
        }

        executeChatCall(apiService.sendOpenAIMessage(request), callback, "OpenAI", logger);
    }

    private static void sendDoubao(ApiConfig apiConfig,
                                   String normalizedContent,
                                   @Nullable String systemPrompt,
                                   @Nullable ApiClient.ChatCallback callback,
                                   Consumer<String> logger) {
        boolean hasSystemPrompt = systemPrompt != null && !systemPrompt.trim().isEmpty();
        if (hasSystemPrompt) {
            logger.accept("[豆包] 开始发送消息（含系统提示）- URL: " + apiConfig.getApiUrl() + ", 模型: " + apiConfig.getModel());
        } else {
            logger.accept("[豆包] 开始发送消息 - URL: " + apiConfig.getApiUrl() + ", 模型: " + apiConfig.getModel());
        }

        if (callback != null) {
            DoubaoApiClient.sendMessage(normalizedContent, systemPrompt, callback);
        }
    }

    private static void executeChatCall(Call<ChatResponse> call,
                                        @Nullable ApiClient.ChatCallback callback,
                                        String apiName,
                                        Consumer<String> logger) {
        NetworkRequestExecutor.executeChatRequest(
                call,
                callback,
                apiName,
                logger
        );
    }

    private static void executeFullResponseCall(Call<ChatResponse> call,
                                                @Nullable ApiClient.ChatResponseCallback callback,
                                                Consumer<String> logger) {
        NetworkRequestExecutor.executeFullResponse(
                call,
                callback,
                logger
        );
    }

    private static void sendCustomFullResponse(ApiClient.ApiService apiService,
                                               String normalizedContent,
                                               long petId,
                                               @Nullable ChatRequest.PetInfo petInfo,
                                               @Nullable List<ChatRequest.Message> historyMessages,
                                               @Nullable ApiClient.ChatResponseCallback callback,
                                               Consumer<String> logger) {
        ChatRequest request = (ChatRequest) ApiRequestFactory.createChatRequest(
                normalizedContent,
                petId,
                petInfo,
                historyMessages,
                null
        );

        logger.accept("发送自定义格式完整响应请求");
        executeFullResponseCall(apiService.sendMessage(request), callback, logger);
    }

    private static void sendOpenAIFullResponse(ApiClient.ApiService apiService,
                                               ApiConfig apiConfig,
                                               String normalizedContent,
                                               @Nullable List<ChatRequest.Message> historyMessages,
                                               @Nullable ApiClient.ChatResponseCallback callback,
                                               Consumer<String> logger) {
        String model = apiConfig.getApiModel();

        OpenAIChatRequest request;
        if (historyMessages == null || historyMessages.isEmpty()) {
            request = (OpenAIChatRequest) ApiRequestFactory.createOpenAIRequest(model, normalizedContent);
        } else {
            List<OpenAIChatRequest.Message> openAIHistory = convertHistory(historyMessages);
            ensureSystemMessage(openAIHistory);
            openAIHistory.add(new OpenAIChatRequest.Message("user", normalizedContent));
            request = new OpenAIChatRequest(model, openAIHistory);
        }

        logger.accept("发送 OpenAI 完整响应请求 - 模型: " + model);
        executeFullResponseCall(apiService.sendOpenAIMessage(request), callback, logger);
    }

    private static void sendDoubaoFullResponse(ApiConfig apiConfig,
                                               String normalizedContent,
                                               @Nullable ApiClient.ChatResponseCallback callback,
                                               Consumer<String> logger) {
        logger.accept("[豆包] 发送完整响应请求 - 模型: " + apiConfig.getModel());
        if (callback == null) {
            return;
        }

        DoubaoApiClient.sendMessage(normalizedContent, null, new ApiClient.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                ChatResponse response = new ChatResponse();
                response.reply = reply;
                response.model = apiConfig.getModel();
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    private static List<OpenAIChatRequest.Message> convertHistory(List<ChatRequest.Message> historyMessages) {
        List<OpenAIChatRequest.Message> openAIHistory = new ArrayList<>(historyMessages.size());
        for (ChatRequest.Message msg : historyMessages) {
            if (msg == null || msg.role == null || msg.content == null) {
                continue;
            }
            openAIHistory.add(new OpenAIChatRequest.Message(msg.role, msg.content));
        }
        return openAIHistory;
    }

    private static void ensureSystemMessage(List<OpenAIChatRequest.Message> messages) {
        for (OpenAIChatRequest.Message message : messages) {
            if ("system".equals(message.role)) {
                return;
            }
        }
        messages.add(0, new OpenAIChatRequest.Message("system", "你是一个友好的虚拟宠物助手。"));
    }
}
