package com.mpcorporation.snapeffect.domain.model

/**
 * 4 nhóm công cụ trên bottom bar của Editor (nút chụp ảnh nằm giữa, không thuộc enum này).
 *
 * Trước đây bottom bar liệt kê phẳng 9 công cụ; giờ gom lại thành 4 nhóm cho gọn:
 * - [FILTER]  : toàn bộ hiệu ứng màu (bộ lọc, nghệ thuật, biến dạng) - xem
 *               [com.mpcorporation.snapeffect.domain.repository.EffectCatalog.filterGroups].
 * - [BEAUTY]  : làm đẹp khuôn mặt (màn Retouch).
 * - [STICKER] : dán sticker lên ảnh.
 * - [EDIT]    : công cụ chỉnh sửa (cắt, chỉnh ảnh, vùng chọn, chữ) - xem [EditTool].
 */
enum class EditorTool {
    FILTER,
    BEAUTY,
    STICKER,
    EDIT,
}

/** Công cụ con trong nhóm [EditorTool.EDIT] (sheet mở ra khi bấm "Chỉnh sửa"). */
enum class EditTool {
    CROP,
    ADJUST,
    SELECTIVE,
    TEXT,
}
