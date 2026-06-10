package com.nonsense.chat.ui.navigation

object Routes {
    const val AUTH = "auth"
    const val CHATS = "chats"

    const val CHAT = "chat/{chatId}"
    fun chat(chatId: String) = "chat/$chatId"

    const val PROFILE = "profile/{uid}"
    fun profile(uid: String) = "profile/$uid"

    const val EDIT_PROFILE = "editProfile"
    const val SETTINGS = "settings"
    const val FRIENDS = "friends"
    const val NEW_CHAT = "newChat"
    const val NEW_GROUP = "newGroup"

    const val GROUP_INFO = "groupInfo/{chatId}"
    fun groupInfo(chatId: String) = "groupInfo/$chatId"

    const val CALL = "call"

    const val ARG_CHAT_ID = "chatId"
    const val ARG_UID = "uid"
}
