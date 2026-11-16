package com.cali.core.ai.anthropic

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// DTO cho POST /v1/messages, anthropic-version 2026-10-21. Chỉ các trường app dùng.

@Serializable
internal data class MessagesRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: List<SystemBlock>,
    val messages: List<MessageParam>,
    val tools: List<ToolParam>,
    @SerialName("tool_choice") val toolChoice: ToolChoice,
    @SerialName("output_config") val outputConfig: OutputConfig? = null,
    /** `"default"` = fallback phía server theo loại từ chối (beta server-side-fallback-2026-07-01). */
    val fallbacks: String? = null,
)

@Serializable
internal data class SystemBlock(
    val text: String,
    @SerialName("cache_control") val cacheControl: CacheControl? = null,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    val type: String = "text"
}

@Serializable
internal data class CacheControl(val type: String = "ephemeral")

@Serializable
internal data class MessageParam(val role: String, val content: List<ContentParam>)

/** Khối nội dung gửi đi; `type` do kotlinx thêm theo [SerialName] (classDiscriminator mặc định là "type"). */
@Serializable
internal sealed interface ContentParam {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : ContentParam

    @Serializable
    @SerialName("image")
    data class Image(val source: ImageSource) : ContentParam
}

@Serializable
internal data class ImageSource(
    val data: String,
    @SerialName("media_type") val mediaType: String = "image/jpeg",
) {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    val type: String = "base64"
}

@Serializable
internal data class ToolParam(
    val name: String,
    val description: String,
    @SerialName("input_schema") val inputSchema: JsonObject,
    val strict: Boolean = true,
)

@Serializable
internal data class ToolChoice(val type: String = "auto")

@Serializable
internal data class OutputConfig(val effort: String)

// --- Response ---

@Serializable
internal data class MessagesResponse(
    val id: String? = null,
    val model: String? = null,
    /** Đọc theo `type`: thinking, text, tool_use, fallback… — loại lạ bị bỏ qua. */
    val content: List<JsonObject> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
    @SerialName("stop_details") val stopDetails: JsonElement? = null,
    val usage: Usage? = null,
)

@Serializable
internal data class Usage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
    @SerialName("cache_read_input_tokens") val cacheReadInputTokens: Int? = null,
    @SerialName("cache_creation_input_tokens") val cacheCreationInputTokens: Int? = null,
)

@Serializable
internal data class ErrorResponse(val error: ErrorBody? = null)

@Serializable
internal data class ErrorBody(val type: String? = null, val message: String? = null)
