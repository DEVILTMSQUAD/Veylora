package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    // Per skill guidelines: use gemini-3.5-flash for general text tasks
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio.")
                )
            }

            try {
                val url = URL("$BASE_URL?key=$apiKey")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connectTimeout = 30000
                    readTimeout = 30000
                    doOutput = true
                    doInput = true
                }

                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }

                val requestJson = JSONObject().apply {
                    put("contents", contentsArray)

                    if (!systemInstruction.isNullOrBlank()) {
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", systemInstruction)
                                })
                            })
                        })
                    }

                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 1024)
                    })
                }

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val jsonResponse = JSONObject(responseText)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            return@withContext Result.success(text.trim())
                        }
                    }
                    Result.failure(Exception("Empty candidate returned from Gemini API"))
                } else {
                    val errorText = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: "HTTP $responseCode"
                    Log.e(TAG, "Gemini API error ($responseCode): $errorText")
                    Result.failure(Exception("Gemini API request failed ($responseCode): $errorText"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception calling Gemini API", e)
                Result.failure(e)
            }
        }

    /**
     * AI-Powered Study Reminder Generator
     * Analyzes student tasks, subjects, upcoming exams, and time of day to craft personalized,
     * highly motivational, and timely reminders in Bengali.
     */
    suspend fun generateSmartReminder(
        studentName: String,
        studentClass: String,
        pendingTasks: List<String>,
        subjectsWithLowProgress: List<String>,
        upcomingExams: List<String>,
        timeOfDay: String = "বর্তমান সময়"
    ): Result<SmartReminderResult> = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("শিক্ষার্থীর তথ্য:")
            appendLine("- নাম: ${if (studentName.isNotBlank()) studentName else "শিক্ষার্থী"}")
            appendLine("- শ্রেণী: ${if (studentClass.isNotBlank()) studentClass else "৯ম-১০ম শ্রেণী"}")
            appendLine("- বর্তমান সময়: $timeOfDay")
            if (pendingTasks.isNotEmpty()) {
                appendLine("- আজকের বাকি টাস্কসমূহ: ${pendingTasks.take(5).joinToString(", ")}")
            } else {
                appendLine("- আজকের সকল টাস্ক ইতিমধ্যে সমাপ্ত হয়েছে অথবা নতুন রুটিন দরকার।")
            }
            if (subjectsWithLowProgress.isNotEmpty()) {
                appendLine("- যেসকল বিষয়ে দ্রুত রিভিশন দরকার: ${subjectsWithLowProgress.take(3).joinToString(", ")}")
            }
            if (upcomingExams.isNotEmpty()) {
                appendLine("- আসন্ন পরীক্ষাসমূহ: ${upcomingExams.take(2).joinToString(", ")}")
            }
            appendLine()
            appendLine("তুমি Veylora অ্যাপের একজন আন্তরিক, অভিজ্ঞ AI স্টাডি মেন্টর।")
            appendLine("শিক্ষার্থীকে সঠিকভাবে সময়মতো পড়ার টেবিলে বসানোর জন্য একটি অনুপ্রেরণাদায়ক ও আকর্ষণীয় রিমাইন্ডার মেসেজ তৈরি করো।")
            appendLine("দয়া করে নিচের নির্দিষ্ট JSON ফরম্যাটে উত্তর দাও:")
            appendLine("{")
            appendLine("  \"title\": \"একটি সংক্ষিপ্ত আকর্ষণীয় রিমাইন্ডার টাইটেল (যেমন: 📚 পদার্থবিজ্ঞান পড়ার মোক্ষম সময়!)\",")
            appendLine("  \"message\": \"২-৩ বাক্যের সুন্দর, ব্যক্তিগতকৃত এবং উদ্বুদ্ধকারী পরামর্শ ও নির্দেশনা।\",")
            appendLine("  \"suggestedAction\": \"নির্দিষ্ট একটি কাজের পরামর্শ (যেমন: গতি অধ্যায়ের অঙ্ক প্র্যাকটিস করো)\"")
            appendLine("}")
            appendLine("শুধুমাত্র ভ্যালিড JSON প্রদান করো, অতিরিক্ত কোনো ব্যাকটিক্স বা লেখা ছাড়া।")
        }

        val result = generateContent(
            prompt = prompt,
            systemInstruction = "You are Veylora AI, a friendly and disciplined personal study mentor for Bangladeshi students (Class 9-10 & HSC). You write heartfelt, encouraging Bengali reminders that help students overcome procrastination and study effectively on time."
        )

        result.fold(
            onSuccess = { rawText ->
                try {
                    val cleanJson = rawText
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    val json = JSONObject(cleanJson)
                    Result.success(
                        SmartReminderResult(
                            title = json.optString("title", "🔔 পড়ার সময় হয়েছে!"),
                            message = json.optString("message", "আজকের পড়ার লক্ষ্যমাত্রা পূরণ করতে এখনই টেবিলে বসো।"),
                            suggestedAction = json.optString("suggestedAction", "আজকের টাস্ক রিভিশন করো")
                        )
                    )
                } catch (e: Exception) {
                    // Fallback to text parsing
                    val lines = rawText.lines().filter { it.isNotBlank() }
                    val title = lines.firstOrNull()?.replace("\"", "") ?: "🔔 Veylora AI পড়ার রিমাইন্ডার"
                    val msg = lines.drop(1).joinToString("\n").replace("\"", "").ifBlank { rawText }
                    Result.success(
                        SmartReminderResult(
                            title = title.take(40),
                            message = msg,
                            suggestedAction = "পড়াশোনা শুরু করুন"
                        )
                    )
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}

data class SmartReminderResult(
    val title: String,
    val message: String,
    val suggestedAction: String
)
