package com.mpcorporation.snapeffect.data.catalog

import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.data.filter.filters.BrightnessFilter
import com.mpcorporation.snapeffect.data.filter.filters.ColorTintFilter
import com.mpcorporation.snapeffect.data.filter.filters.ContrastFilter
import com.mpcorporation.snapeffect.data.filter.filters.CrosshatchFilter
import com.mpcorporation.snapeffect.data.filter.filters.EmbossFilter
import com.mpcorporation.snapeffect.data.filter.filters.ExposureFilter
import com.mpcorporation.snapeffect.data.filter.filters.GammaFilter
import com.mpcorporation.snapeffect.data.filter.filters.GaussianBlurFilter
import com.mpcorporation.snapeffect.data.filter.filters.GrayscaleFilter
import com.mpcorporation.snapeffect.data.filter.filters.HalftoneFilter
import com.mpcorporation.snapeffect.data.filter.filters.HueFilter
import com.mpcorporation.snapeffect.data.filter.filters.LuminanceThresholdFilter
import com.mpcorporation.snapeffect.data.filter.filters.PixelateFilter
import com.mpcorporation.snapeffect.data.filter.filters.PresetFilter
import com.mpcorporation.snapeffect.data.filter.filters.SaturationFilter
import com.mpcorporation.snapeffect.data.filter.filters.SepiaFilter
import com.mpcorporation.snapeffect.data.filter.filters.SmoothToonFilter
import com.mpcorporation.snapeffect.data.filter.filters.SobelFilter
import com.mpcorporation.snapeffect.data.filter.filters.SolarizeFilter
import com.mpcorporation.snapeffect.data.filter.filters.SwirlFilter
import com.mpcorporation.snapeffect.data.filter.filters.VignetteFilter
import com.mpcorporation.snapeffect.data.filter.filters.WhiteBalanceFilter
import com.mpcorporation.snapeffect.data.filter.filters.ZoomBlurFilter
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import com.mpcorporation.snapeffect.domain.model.BottomNavItem
import com.mpcorporation.snapeffect.domain.model.EditTool
import com.mpcorporation.snapeffect.domain.model.EditToolItem
import com.mpcorporation.snapeffect.domain.model.EditorTool
import com.mpcorporation.snapeffect.domain.model.EffectGroup
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.domain.model.ScaleCrop
import com.mpcorporation.snapeffect.domain.model.Sticker
import com.mpcorporation.snapeffect.domain.model.StickerGroup
import com.mpcorporation.snapeffect.domain.model.UserPreset
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EffectCatalogImpl @Inject constructor() : EffectCatalog {

    // ---------------------------------------------------------------------
    // Bottom bar: 4 nhóm (nút chụp ảnh nằm giữa, dựng riêng trong EditorBottomBar)
    // ---------------------------------------------------------------------

    override fun bottomNavItems(): List<BottomNavItem> = listOf(
        BottomNavItem(EditorTool.FILTER, R.drawable.bot_nav_looks, "Bộ lọc"),
        BottomNavItem(EditorTool.BEAUTY, R.drawable.bot_nav_retouch, "Làm đẹp"),
        BottomNavItem(EditorTool.STICKER, R.drawable.bot_nav_sticker, "Sticker"),
        BottomNavItem(EditorTool.EDIT, R.drawable.bot_nav_edit, "Chỉnh sửa")
    )

    override fun editTools(): List<EditToolItem> = listOf(
        EditToolItem(EditTool.CROP, R.drawable.bot_nav_crop, "Cắt & xoay"),
        EditToolItem(EditTool.ADJUST, R.drawable.bot_nav_adjust, "Chỉnh ảnh"),
        EditToolItem(EditTool.SELECTIVE, R.drawable.bot_nav_selective, "Vùng chọn"),
        EditToolItem(EditTool.TEXT, R.drawable.bot_nav_text, "Chữ")
    )

    // ---------------------------------------------------------------------
    // Nhóm "Bộ lọc": 20 bộ lọc màu chia 4 phong cách + nghệ thuật + biến dạng
    // ---------------------------------------------------------------------

    override fun filterGroups(): List<EffectGroup> = listOf(
        EffectGroup("Chân dung", portraitFilters()),
        EffectGroup("Điện ảnh", cinematicFilters()),
        EffectGroup("Cổ điển", vintageFilters()),
        EffectGroup("Đen trắng", monoFilters()),
        EffectGroup("Nghệ thuật", artEffects()),
        EffectGroup("Biến dạng", distortEffects())
    )

    /** Tông nhẹ, hợp ảnh người - không đổi màu da quá tay. */
    private fun portraitFilters(): List<EffectItem> = listOf(
        EffectItem("Tự nhiên", R.drawable.preset_fade, PresetFilter(
            BrightnessFilter(0.04f), ContrastFilter(1.05f), SaturationFilter(1.06f)
        )),
        EffectItem("Da sứ", R.drawable.preset_matte, PresetFilter(
            BrightnessFilter(0.1f), ContrastFilter(0.95f), SaturationFilter(0.95f),
            ColorTintFilter(redOffset = 6f, greenOffset = 3f)
        )),
        EffectItem("Hồng đào", R.drawable.preset_warm, PresetFilter(
            ColorTintFilter(redScale = 1.08f, blueScale = 1.03f, redOffset = 6f),
            SaturationFilter(1.1f), BrightnessFilter(0.05f)
        )),
        EffectItem("Nắng mai", R.drawable.preset_golden, PresetFilter(
            WhiteBalanceFilter(3600f), BrightnessFilter(0.07f), SaturationFilter(1.15f)
        )),
        EffectItem("Ngọt ngào", R.drawable.preset_fade, PresetFilter(
            ContrastFilter(0.9f), BrightnessFilter(0.12f), SaturationFilter(1.12f),
            ColorTintFilter(redOffset = 10f, blueOffset = 8f)
        ))
    )

    /** Tương phản mạnh + vignette - kiểu màu phim. */
    private fun cinematicFilters(): List<EffectItem> = listOf(
        EffectItem("Điện ảnh", R.drawable.preset_drama, PresetFilter(
            ColorTintFilter(redScale = 1.1f, blueScale = 1.05f, blueOffset = 10f),
            ContrastFilter(1.2f), SaturationFilter(0.95f), VignetteFilter(0.35f)
        )),
        EffectItem("Kịch tính", R.drawable.preset_drama, PresetFilter(
            ContrastFilter(1.5f), SaturationFilter(1.4f), BrightnessFilter(-0.05f),
            VignetteFilter(0.3f)
        )),
        EffectItem("Xanh lạnh", R.drawable.preset_cool, PresetFilter(
            WhiteBalanceFilter(7800f), SaturationFilter(1.1f), ContrastFilter(1.15f)
        )),
        EffectItem("Hoàng hôn", R.drawable.preset_warm, PresetFilter(
            WhiteBalanceFilter(2800f), SaturationFilter(1.35f), BrightnessFilter(0.05f),
            ColorTintFilter(redOffset = 8f)
        )),
        EffectItem("Đêm noir", R.drawable.preset_bw, PresetFilter(
            SaturationFilter(0.45f), ContrastFilter(1.55f),
            ColorTintFilter(blueScale = 1.08f), VignetteFilter(0.5f)
        ))
    )

    /** Ám vàng / bạc màu - kiểu ảnh film cũ. */
    private fun vintageFilters(): List<EffectItem> = listOf(
        EffectItem("Vintage", R.drawable.preset_vintage, PresetFilter(
            SepiaFilter(0.35f), BrightnessFilter(-0.03f), ContrastFilter(1.05f)
        )),
        EffectItem("Phai màu", R.drawable.preset_fade, PresetFilter(
            SaturationFilter(0.6f), BrightnessFilter(0.1f), ContrastFilter(0.85f)
        )),
        EffectItem("Polaroid", R.drawable.preset_matte, PresetFilter(
            ColorTintFilter(redOffset = 14f, greenOffset = 9f, blueOffset = 4f),
            ContrastFilter(0.9f), SaturationFilter(0.85f)
        )),
        EffectItem("Retro 70s", R.drawable.preset_vintage, PresetFilter(
            SepiaFilter(0.2f),
            ColorTintFilter(redScale = 1.12f, greenScale = 1.02f, blueScale = 0.9f),
            ContrastFilter(1.1f)
        )),
        EffectItem("Mờ sương", R.drawable.preset_matte, PresetFilter(
            ContrastFilter(0.88f), SaturationFilter(0.8f), BrightnessFilter(0.08f),
            ColorTintFilter(blueOffset = 12f)
        ))
    )

    /** Đen trắng và các tông đậm. */
    private fun monoFilters(): List<EffectItem> = listOf(
        EffectItem("Trắng đen", R.drawable.preset_bw, PresetFilter(
            GrayscaleFilter(), ContrastFilter(1.4f)
        )),
        EffectItem("Bạc", R.drawable.preset_bw, PresetFilter(
            GrayscaleFilter(), BrightnessFilter(0.08f), ContrastFilter(1.15f),
            ColorTintFilter(blueScale = 1.05f)
        )),
        EffectItem("Than chì", R.drawable.preset_bw, PresetFilter(
            GrayscaleFilter(), ContrastFilter(1.8f), VignetteFilter(0.4f)
        )),
        EffectItem("Rực rỡ", R.drawable.preset_drama, PresetFilter(
            SaturationFilter(1.6f), ContrastFilter(1.2f), BrightnessFilter(0.02f)
        )),
        EffectItem("Nắng vàng", R.drawable.preset_golden, PresetFilter(
            WhiteBalanceFilter(3000f), SaturationFilter(1.5f),
            BrightnessFilter(0.08f), ContrastFilter(1.1f)
        ))
    )

    /** 20 bộ lọc phẳng - khay look của Camera lấy vài cái đầu. */
    override fun presetEffects(): List<EffectItem> =
        portraitFilters() + cinematicFilters() + vintageFilters() + monoFilters()

    // ---------------------------------------------------------------------
    // Sticker: 140 cái = 120 emoji (10 nhóm x 12) + 20 hình vector tự vẽ.
    //
    // [Sticker.Emoji] vẽ bằng font emoji của máy (Canvas.drawText trong BurnStickersUseCase,
    // KHÔNG qua EmojiCompat) nên emoji mới hơn font hệ thống sẽ ra ô vuông. minSdk 28 = Android 9
    // = Emoji 11.0 -> chỉ dùng emoji từ Emoji 11.0 trở xuống. Thêm emoji mới phải theo luật này.
    //
    // [Sticker.Vector] là drawable `stk_*` app tự vẽ - không dính giới hạn font nào.
    // ---------------------------------------------------------------------

    override fun stickerGroups(): List<StickerGroup> = listOf(
        StickerGroup("Trang trí", decorStickers()),
        StickerGroup("Cảm xúc", emotionStickers()),
        StickerGroup("Tình yêu", loveStickers()),
        StickerGroup("Dễ thương", cuteStickers()),
        StickerGroup("Lễ hội", partyStickers()),
        StickerGroup("Thiên nhiên", natureStickers()),
        StickerGroup("Đồ ăn", foodStickers()),
        StickerGroup("Du lịch", travelStickers()),
        StickerGroup("Bàn tay", handStickers()),
        StickerGroup("Phụ kiện", accessoryStickers()),
        StickerGroup("Ký hiệu", symbolStickers())
    )

    /**
     * Hình vector tự vẽ - loại emoji bàn phím không có: khung ảnh, nét vẽ tay chú thích, hiệu
     * ứng ánh sáng. Màu lấy theo palette Beauty Camera; hình nào cũng có viền trắng hoặc nét
     * trắng lót để nổi trên nền ảnh bất kỳ.
     */
    private fun decorStickers(): List<Sticker> = listOf(
        Sticker.Vector("neon_heart", R.drawable.stk_neon_heart),
        Sticker.Vector("speech_bubble", R.drawable.stk_speech_bubble),
        Sticker.Vector("thought_cloud", R.drawable.stk_thought_cloud),
        Sticker.Vector("sparkle", R.drawable.stk_sparkle),
        Sticker.Vector("sparkle_cluster", R.drawable.stk_sparkle_cluster),
        Sticker.Vector("arrow_doodle", R.drawable.stk_arrow_doodle),
        Sticker.Vector("circle_highlight", R.drawable.stk_circle_highlight),
        Sticker.Vector("underline_stroke", R.drawable.stk_underline_stroke),
        Sticker.Vector("wave_line", R.drawable.stk_wave_line),
        Sticker.Vector("ribbon_banner", R.drawable.stk_ribbon_banner),
        Sticker.Vector("price_tag", R.drawable.stk_price_tag),
        Sticker.Vector("polaroid_frame", R.drawable.stk_polaroid_frame),
        Sticker.Vector("film_strip", R.drawable.stk_film_strip),
        Sticker.Vector("tape_strip", R.drawable.stk_tape_strip),
        Sticker.Vector("sun_rays", R.drawable.stk_sun_rays),
        Sticker.Vector("bow", R.drawable.stk_bow),
        Sticker.Vector("crown_gems", R.drawable.stk_crown),
        Sticker.Vector("halo_ring", R.drawable.stk_halo_ring),
        Sticker.Vector("dotted_circle", R.drawable.stk_dotted_circle),
        Sticker.Vector("lens_flare", R.drawable.stk_lens_flare)
    )

    private fun emotionStickers(): List<Sticker> = listOf(
        Sticker.Emoji("love_eyes", "😍"),
        Sticker.Emoji("laugh", "😂"),
        Sticker.Emoji("cool", "😎"),
        Sticker.Emoji("hug", "🥰"),
        Sticker.Emoji("wink", "😜"),
        Sticker.Emoji("party_face", "🥳"),
        Sticker.Emoji("smile", "😊"),
        Sticker.Emoji("star_struck", "🤩"),
        Sticker.Emoji("kiss_face", "😘"),
        Sticker.Emoji("hugging", "🤗"),
        Sticker.Emoji("pleading", "🥺"),
        Sticker.Emoji("sleepy", "😴")
    )

    private fun loveStickers(): List<Sticker> = listOf(
        Sticker.Emoji("heart", "❤️"),
        Sticker.Emoji("kiss", "💋"),
        Sticker.Emoji("hearts", "💕"),
        Sticker.Emoji("rose", "🌹"),
        Sticker.Emoji("ring", "💍"),
        Sticker.Emoji("sparkling_heart", "💖"),
        Sticker.Emoji("cupid", "💘"),
        Sticker.Emoji("gift_heart", "💝"),
        Sticker.Emoji("growing_heart", "💗"),
        Sticker.Emoji("revolving_hearts", "💞"),
        Sticker.Emoji("heart_cat", "😻"),
        Sticker.Emoji("bouquet", "💐")
    )

    private fun cuteStickers(): List<Sticker> = listOf(
        Sticker.Emoji("cat", "🐱"),
        Sticker.Emoji("dog", "🐶"),
        Sticker.Emoji("rabbit", "🐰"),
        Sticker.Emoji("butterfly", "🦋"),
        Sticker.Emoji("blossom", "🌸"),
        Sticker.Emoji("bear", "🐻"),
        Sticker.Emoji("panda", "🐼"),
        Sticker.Emoji("fox", "🦊"),
        Sticker.Emoji("chick", "🐥"),
        Sticker.Emoji("penguin", "🐧"),
        Sticker.Emoji("unicorn", "🦄"),
        Sticker.Emoji("koala", "🐨")
    )

    private fun partyStickers(): List<Sticker> = listOf(
        Sticker.Emoji("party", "🎉"),
        Sticker.Emoji("cake", "🎂"),
        Sticker.Emoji("crown", "👑"),
        Sticker.Emoji("sparkles", "✨"),
        Sticker.Emoji("rainbow", "🌈"),
        Sticker.Emoji("balloon", "🎈"),
        Sticker.Emoji("gift", "🎁"),
        Sticker.Emoji("confetti", "🎊"),
        Sticker.Emoji("champagne", "🍾"),
        Sticker.Emoji("cheers", "🥂"),
        Sticker.Emoji("christmas_tree", "🎄"),
        Sticker.Emoji("pumpkin", "🎃")
    )

    private fun natureStickers(): List<Sticker> = listOf(
        Sticker.Emoji("tulip", "🌷"),
        Sticker.Emoji("sunflower", "🌻"),
        Sticker.Emoji("hibiscus", "🌺"),
        Sticker.Emoji("daisy", "🌼"),
        Sticker.Emoji("clover", "🍀"),
        Sticker.Emoji("herb", "🌿"),
        Sticker.Emoji("palm", "🌴"),
        Sticker.Emoji("maple_leaf", "🍁"),
        Sticker.Emoji("cactus", "🌵"),
        Sticker.Emoji("moon", "🌙"),
        Sticker.Emoji("star", "⭐"),
        Sticker.Emoji("sun", "☀️")
    )

    private fun foodStickers(): List<Sticker> = listOf(
        Sticker.Emoji("burger", "🍔"),
        Sticker.Emoji("pizza", "🍕"),
        Sticker.Emoji("fries", "🍟"),
        Sticker.Emoji("donut", "🍩"),
        Sticker.Emoji("ice_cream", "🍦"),
        Sticker.Emoji("shortcake", "🍰"),
        Sticker.Emoji("strawberry", "🍓"),
        Sticker.Emoji("watermelon", "🍉"),
        Sticker.Emoji("cherries", "🍒"),
        Sticker.Emoji("peach", "🍑"),
        Sticker.Emoji("coffee", "☕"),
        Sticker.Emoji("drink", "🥤")
    )

    private fun travelStickers(): List<Sticker> = listOf(
        Sticker.Emoji("airplane", "✈️"),
        Sticker.Emoji("car", "🚗"),
        Sticker.Emoji("beach", "🏖️"),
        Sticker.Emoji("map", "🗺️"),
        Sticker.Emoji("backpack", "🎒"),
        Sticker.Emoji("camera", "📷"),
        Sticker.Emoji("luggage", "🧳"),
        Sticker.Emoji("bicycle", "🚲"),
        Sticker.Emoji("parasol", "⛱️"),
        Sticker.Emoji("tower", "🗼"),
        Sticker.Emoji("island", "🏝️"),
        Sticker.Emoji("ferris_wheel", "🎡")
    )

    private fun handStickers(): List<Sticker> = listOf(
        Sticker.Emoji("thumbs_up", "👍"),
        Sticker.Emoji("ok_hand", "👌"),
        Sticker.Emoji("victory", "✌️"),
        Sticker.Emoji("love_you", "🤟"),
        Sticker.Emoji("clap", "👏"),
        Sticker.Emoji("raised_hands", "🙌"),
        Sticker.Emoji("call_me", "🤙"),
        Sticker.Emoji("handshake", "🤝"),
        Sticker.Emoji("raised_hand", "✋"),
        Sticker.Emoji("wave", "👋"),
        Sticker.Emoji("pray", "🙏"),
        Sticker.Emoji("muscle", "💪")
    )

    private fun accessoryStickers(): List<Sticker> = listOf(
        Sticker.Emoji("sunglasses", "🕶️"),
        Sticker.Emoji("glasses", "👓"),
        Sticker.Emoji("sun_hat", "👒"),
        Sticker.Emoji("top_hat", "🎩"),
        Sticker.Emoji("cap", "🧢"),
        Sticker.Emoji("lipstick", "💄"),
        Sticker.Emoji("handbag", "👜"),
        Sticker.Emoji("heels", "👠"),
        Sticker.Emoji("ribbon", "🎀"),
        Sticker.Emoji("gem", "💎"),
        Sticker.Emoji("watch", "⌚"),
        Sticker.Emoji("scarf", "🧣")
    )

    private fun symbolStickers(): List<Sticker> = listOf(
        Sticker.Emoji("dizzy", "💫"),
        Sticker.Emoji("zap", "⚡"),
        Sticker.Emoji("boom", "💥"),
        Sticker.Emoji("hundred", "💯"),
        Sticker.Emoji("fire", "🔥"),
        Sticker.Emoji("zzz", "💤"),
        Sticker.Emoji("speech", "💬"),
        Sticker.Emoji("thought", "💭"),
        Sticker.Emoji("note", "🎵"),
        Sticker.Emoji("notes", "🎶"),
        Sticker.Emoji("trophy", "🏆"),
        Sticker.Emoji("target", "🎯")
    )

    // ---------------------------------------------------------------------
    // Hiệu ứng có tham số (slider)
    // ---------------------------------------------------------------------

    override fun adjustEffects(): List<EffectItem> = listOf(
        EffectItem(
            name = "Độ sáng",
            filterIconRes = R.drawable.adjust_brightness,
            filter = BrightnessFilter(0f),
            label = "Độ sáng",
            hasParameter = true,
            min = -1f, max = 1f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as BrightnessFilter).brightness = v }
        ),
        EffectItem(
            name = "Tương phản",
            filterIconRes = R.drawable.adjust_contrast,
            filter = ContrastFilter(1f),
            label = "Tương phản",
            hasParameter = true,
            min = 0f, max = 4f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as ContrastFilter).contrast = v }
        ),
        EffectItem(
            name = "Bão hòa",
            filterIconRes = R.drawable.adjust_saturation,
            filter = SaturationFilter(1f),
            label = "Bão hòa",
            hasParameter = true,
            min = 0f, max = 2f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as SaturationFilter).saturation = v }
        ),
        EffectItem(
            name = "Màu Hue",
            filterIconRes = R.drawable.adjust_hue,
            filter = HueFilter(0f),
            label = "Hue",
            hasParameter = true,
            min = 0f, max = 360f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as HueFilter).hue = v }
        ),
        EffectItem(
            name = "Gamma",
            filterIconRes = R.drawable.adjust_gamma,
            filter = GammaFilter(1f),
            label = "Gamma",
            hasParameter = true,
            min = 0.1f, max = 3f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as GammaFilter).gamma = v }
        ),
        EffectItem(
            name = "Phơi sáng",
            filterIconRes = R.drawable.adjust_exposure,
            filter = ExposureFilter(0f),
            label = "Phơi sáng",
            hasParameter = true,
            min = -10f, max = 10f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as ExposureFilter).exposure = v }
        ),
        EffectItem(
            name = "Cân bằng trắng",
            filterIconRes = R.drawable.adjust_white_balance,
            filter = WhiteBalanceFilter(5000f),
            label = "Cân bằng trắng",
            hasParameter = true,
            min = 2000f, max = 8000f, defaultValue = 5000f,
            parameterApplier = { f, v -> (f as WhiteBalanceFilter).temperature = v }
        ),
        EffectItem(
            name = "Ngưỡng hóa sáng",
            filterIconRes = R.drawable.adjust_luminance,
            filter = LuminanceThresholdFilter(0.5f),
            label = "Ngưỡng",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.5f,
            parameterApplier = { f, v -> (f as LuminanceThresholdFilter).threshold = v }
        ),
        EffectItem(
            name = "Tối góc",
            filterIconRes = R.drawable.adjust_luminance,
            filter = VignetteFilter(0.4f),
            label = "Độ tối góc",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.4f,
            parameterApplier = { f, v -> (f as VignetteFilter).strength = v }
        )
    )

    override fun artEffects(): List<EffectItem> = listOf(
        EffectItem(
            name = "Đơn sắc",
            filterIconRes = R.drawable.art_gray_scale,
            filter = GrayscaleFilter()
        ),
        EffectItem(
            name = "Nâu cổ",
            filterIconRes = R.drawable.art_vintage,
            filter = SepiaFilter(0.5f),
            label = "Cường độ",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.5f,
            parameterApplier = { f, v -> (f as SepiaFilter).intensity = v }
        ),
        EffectItem(
            name = "Hoạt hình",
            filterIconRes = R.drawable.art_smooth_toon,
            filter = SmoothToonFilter(0.6f),
            label = "Độ mượt",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.6f,
            parameterApplier = { f, v -> (f as SmoothToonFilter).threshold = v }
        ),
        EffectItem(
            name = "Chấm tròn nửa tông",
            filterIconRes = R.drawable.art_half_tone,
            filter = HalftoneFilter(0.01f),
            label = "Pixel",
            hasParameter = true,
            min = 0.001f, max = 0.05f, defaultValue = 0.01f,
            parameterApplier = { f, v -> (f as HalftoneFilter).fractionalWidth = v }
        ),
        EffectItem(
            name = "Gạch chéo",
            filterIconRes = R.drawable.art_cross_hatch,
            filter = CrosshatchFilter(0.01f),
            label = "Khoảng cách",
            hasParameter = true,
            min = 0.01f, max = 0.1f, defaultValue = 0.01f,
            parameterApplier = { f, v -> (f as CrosshatchFilter).spacing = v }
        ),
        EffectItem(
            name = "Nổi 3D",
            filterIconRes = R.drawable.art_emboss,
            filter = EmbossFilter(1f),
            label = "Cường độ",
            hasParameter = true,
            min = 0f, max = 5f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as EmbossFilter).intensity = v }
        ),
        EffectItem(
            name = "Đảo ngược vùng sáng",
            filterIconRes = R.drawable.art_solarize,
            filter = SolarizeFilter(0.2f),
            label = "Cường độ",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.2f,
            parameterApplier = { f, v -> (f as SolarizeFilter).threshold = v }
        ),
        EffectItem(
            name = "Sketch",
            filterIconRes = R.drawable.art_sobel,
            filter = SobelFilter(0.8f),
            label = "Biên",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.8f,
            parameterApplier = { f, v -> (f as SobelFilter).threshold = v }
        )
    )

    override fun distortEffects(): List<EffectItem> = listOf(
        EffectItem(
            name = "Khối pixel",
            filterIconRes = R.drawable.distort_pixel,
            filter = PixelateFilter(7f),
            label = "Pixel",
            hasParameter = true,
            min = 1f, max = 100f, defaultValue = 7f,
            parameterApplier = { f, v -> (f as PixelateFilter).pixelSize = v }
        ),
        EffectItem(
            name = "Làm mờ",
            filterIconRes = R.drawable.distort_gaussian,
            filter = GaussianBlurFilter(0f),
            label = "Độ mờ",
            hasParameter = true,
            min = 0f, max = 10f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as GaussianBlurFilter).blurSize = v }
        ),
        EffectItem(
            name = "Xoáy hình ảnh",
            filterIconRes = R.drawable.distort_swirl,
            filter = SwirlFilter(0.1f),
            label = "Độ xoáy",
            hasParameter = true,
            min = 0f, max = 2f, defaultValue = 0.1f,
            parameterApplier = { f, v -> (f as SwirlFilter).angle = v }
        ),
        EffectItem(
            name = "Mờ zoom trung tâm",
            filterIconRes = R.drawable.distort_zoom_blur,
            filter = ZoomBlurFilter(2f),
            label = "Kích thước mờ",
            hasParameter = true,
            min = 0f, max = 2f, defaultValue = 2f,
            parameterApplier = { f, v -> (f as ZoomBlurFilter).blurSize = v }
        )
    )

    override fun cropRatios(): List<ScaleCrop> = listOf(
        ScaleCrop(R.drawable.crop_1_1, "1:1", 1f, 1f),
        ScaleCrop(R.drawable.crop_3_2_24px, "3:2", 3f, 2f),
        ScaleCrop(R.drawable.crop_5_4_24px, "5:4", 5f, 4f),
        ScaleCrop(R.drawable.crop_7_5_24px, "7:5", 7f, 5f),
        ScaleCrop(R.drawable.crop_9_16_24px, "9:16", 9f, 16f),
        ScaleCrop(R.drawable.crop_16_9_24px, "16:9", 16f, 9f)
    )

    override fun buildFilterFromUserPreset(preset: UserPreset): ImageFilter = PresetFilter(
        BrightnessFilter(preset.brightness),
        ContrastFilter(preset.contrast),
        SaturationFilter(preset.saturation),
        HueFilter(preset.hue),
        ExposureFilter(preset.exposure),
        WhiteBalanceFilter(preset.whiteBalance),
        GammaFilter(preset.gamma)
    )

    override fun adjustmentFilter(
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): ImageFilter = PresetFilter(
        BrightnessFilter(brightness),
        ContrastFilter(contrast),
        SaturationFilter(saturation)
    )
}
