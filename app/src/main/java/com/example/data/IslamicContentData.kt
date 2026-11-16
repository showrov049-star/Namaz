package com.example.data

object IslamicContentData {

    val zikrOptions: List<ZikrOption> = listOf(
        ZikrOption(
            id = "subhanallah",
            arabic = "سُبْحَانَ اللَّهِ",
            transliteration = "SubhanAllah",
            meaningEn = "Glory be to Allah",
            meaningBn = "আল্লাহ পবিত্র ও মহান",
            defaultTarget = 33
        ),
        ZikrOption(
            id = "alhamdulillah",
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            meaningEn = "All praise is for Allah",
            meaningBn = "সকল প্রশংসা আল্লাহর জন্য",
            defaultTarget = 33
        ),
        ZikrOption(
            id = "allahuakbar",
            arabic = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            meaningEn = "Allah is the Greatest",
            meaningBn = "আল্লাহ সর্বশ্রেষ্ঠ",
            defaultTarget = 34
        ),
        ZikrOption(
            id = "astaghfirullah",
            arabic = "أَسْتَغْفِرُ اللَّهَ",
            transliteration = "Astaghfirullah",
            meaningEn = "I seek forgiveness from Allah",
            meaningBn = "আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি",
            defaultTarget = 100
        ),
        ZikrOption(
            id = "tahlil",
            arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ",
            transliteration = "La ilaha illallah",
            meaningEn = "There is no deity worthy of worship except Allah",
            meaningBn = "আল্লাহ ব্যতীত কোনো উপাস্য নেই",
            defaultTarget = 100
        ),
        ZikrOption(
            id = "tasbih_bihamdihi",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            meaningEn = "Glory and praise be to Allah",
            meaningBn = "আল্লাহর পবিত্রতা ও প্রশংসা ঘোষণা করছি",
            defaultTarget = 100
        )
    )

    private fun alafasyUrl(globalAyah: Int): String =
        "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$globalAyah.mp3"

    val offlineSurahs: List<QuranSurahDetail> = listOf(
        QuranSurahDetail(
            summary = QuranSurahSummary(
                number = 1,
                arabicName = "سُورَةُ ٱلْفَاتِحَةِ",
                englishName = "Al-Fatihah",
                englishTranslation = "The Opening",
                banglaName = "সূরা আল-ফাতিহা",
                ayahCount = 7,
                revelationType = "Meccan"
            ),
            isOfflineVerified = true,
            ayahs = listOf(
                QuranAyah(
                    numberInSurah = 1,
                    globalNumber = 1,
                    arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    banglaTranslation = "শুরু করছি আল্লাহর নামে যিনি পরম করুণাময়, অতি দয়ালু।",
                    transliteration = "Bismillahir-Rahmanir-Rahim",
                    audioUrl = alafasyUrl(1)
                ),
                QuranAyah(
                    numberInSurah = 2,
                    globalNumber = 2,
                    arabicText = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                    banglaTranslation = "যাবতীয় প্রশংসা আল্লাহ তা'আলার যিনি সকল সৃষ্টি জগতের পালনকর্তা।",
                    transliteration = "Alhamdu lillahi Rabbil-'alamin",
                    audioUrl = alafasyUrl(2)
                ),
                QuranAyah(
                    numberInSurah = 3,
                    globalNumber = 3,
                    arabicText = "الرَّحْمَٰنِ الرَّحِيمِ",
                    banglaTranslation = "যিনি নিতান্ত মেহেরবান ও দয়ালু।",
                    transliteration = "Ar-Rahmanir-Rahim",
                    audioUrl = alafasyUrl(3)
                ),
                QuranAyah(
                    numberInSurah = 4,
                    globalNumber = 4,
                    arabicText = "مَالِكِ يَوْمِ الدِّينِ",
                    banglaTranslation = "যিনি বিচার দিনের মালিক।",
                    transliteration = "Maliki yawmid-din",
                    audioUrl = alafasyUrl(4)
                ),
                QuranAyah(
                    numberInSurah = 5,
                    globalNumber = 5,
                    arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                    banglaTranslation = "আমরা একমাত্র তোমারই ইবাদত করি এবং শুধুমাত্র তোমারই সাহায্য প্রার্থনা করি।",
                    transliteration = "Iyyaka na'budu wa iyyaka nasta'in",
                    audioUrl = alafasyUrl(5)
                ),
                QuranAyah(
                    numberInSurah = 6,
                    globalNumber = 6,
                    arabicText = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                    banglaTranslation = "আমাদেরকে সরল পথ দেখাও।",
                    transliteration = "Ihdinas-siratal-mustaqim",
                    audioUrl = alafasyUrl(6)
                ),
                QuranAyah(
                    numberInSurah = 7,
                    globalNumber = 7,
                    arabicText = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                    banglaTranslation = "সে সমস্ত লোকের পথ, যাদেরকে তুমি নেয়ামত দান করেছ। তাদের পথ নয়, যাদের প্রতি তোমার গজব নাযিল হয়েছে এবং যারা পথভ্রষ্ট হয়েছে।",
                    transliteration = "Siratal-ladhina an'amta 'alayhim ghayril-maghdubi 'alayhim wa lad-dallin",
                    audioUrl = alafasyUrl(7)
                )
            )
        ),
        QuranSurahDetail(
            summary = QuranSurahSummary(
                number = 103,
                arabicName = "سُورَةُ ٱلْعَصْرِ",
                englishName = "Al-Asr",
                englishTranslation = "The Declining Day",
                banglaName = "সূরা আল-আসর",
                ayahCount = 3,
                revelationType = "Meccan"
            ),
            isOfflineVerified = true,
            ayahs = listOf(
                QuranAyah(
                    numberInSurah = 1,
                    globalNumber = 6177,
                    arabicText = "وَالْعَصْرِ",
                    banglaTranslation = "সময়ের কসম,",
                    transliteration = "Wal-'asr",
                    audioUrl = alafasyUrl(6177)
                ),
                QuranAyah(
                    numberInSurah = 2,
                    globalNumber = 6178,
                    arabicText = "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ",
                    banglaTranslation = "নিশ্চয় মানুষ ক্ষতির মধ্যে রয়েছে।",
                    transliteration = "Innal-insana lafi khusr",
                    audioUrl = alafasyUrl(6178)
                ),
                QuranAyah(
                    numberInSurah = 3,
                    globalNumber = 6179,
                    arabicText = "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ",
                    banglaTranslation = "তারা ব্যতীত, যারা ঈমান এনেছে ও সৎকর্ম করেছে এবং পরস্পরকে সত্যের উপদেশ দিয়েছে ও পরস্পরকে ধৈর্যের উপদেশ দিয়েছে।",
                    transliteration = "Illal-ladhina amanu wa 'amilus-salihati wa tawasaw bil-haqqi wa tawasaw bis-sabr",
                    audioUrl = alafasyUrl(6179)
                )
            )
        ),
        QuranSurahDetail(
            summary = QuranSurahSummary(
                number = 108,
                arabicName = "سُورَةُ ٱلْكَوْثَرِ",
                englishName = "Al-Kawthar",
                englishTranslation = "The Abundance",
                banglaName = "সূরা আল-কাওসার",
                ayahCount = 3,
                revelationType = "Meccan"
            ),
            isOfflineVerified = true,
            ayahs = listOf(
                QuranAyah(
                    numberInSurah = 1,
                    globalNumber = 6205,
                    arabicText = "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ",
                    banglaTranslation = "নিশ্চয় আমি আপনাকে কাওসার দান করেছি।",
                    transliteration = "Inna a'taynakal-kawthar",
                    audioUrl = alafasyUrl(6205)
                ),
                QuranAyah(
                    numberInSurah = 2,
                    globalNumber = 6206,
                    arabicText = "فَصَلِّ لِرَبِّكَ وَانْحَرْ",
                    banglaTranslation = "অতএব আপনার পালনকর্তার উদ্দেশ্যে নামাজ পড়ুন এবং কোরবানী করুন।",
                    transliteration = "Fasalli li-rabbika wanhar",
                    audioUrl = alafasyUrl(6206)
                ),
                QuranAyah(
                    numberInSurah = 3,
                    globalNumber = 6207,
                    arabicText = "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
                    banglaTranslation = "নিশ্চয় আপনার শত্রুই নির্বংশ।",
                    transliteration = "Inna shani'aka huwal-abtar",
                    audioUrl = alafasyUrl(6207)
                )
            )
        ),
        QuranSurahDetail(
            summary = QuranSurahSummary(
                number = 112,
                arabicName = "سُورَةُ ٱلْإِخْلَاصِ",
                englishName = "Al-Ikhlas",
                englishTranslation = "The Sincerity",
                banglaName = "সূরা আল-ইখলাস",
                ayahCount = 4,
                revelationType = "Meccan"
            ),
            isOfflineVerified = true,
            ayahs = listOf(
                QuranAyah(
                    numberInSurah = 1,
                    globalNumber = 6222,
                    arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ",
                    banglaTranslation = "বলুন, তিনি আল্লাহ, একক ও অদ্বিতীয়।",
                    transliteration = "Qul huwa Allahu ahad",
                    audioUrl = alafasyUrl(6222)
                ),
                QuranAyah(
                    numberInSurah = 2,
                    globalNumber = 6223,
                    arabicText = "اللَّهُ الصَّمَدُ",
                    banglaTranslation = "আল্লাহ অমুখাপেক্ষী।",
                    transliteration = "Allahus-Samad",
                    audioUrl = alafasyUrl(6223)
                ),
                QuranAyah(
                    numberInSurah = 3,
                    globalNumber = 6224,
                    arabicText = "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                    banglaTranslation = "তিনি কাউকে জন্ম দেননি এবং কেউ তাঁকে জন্ম দেয়নি।",
                    transliteration = "Lam yalid wa lam yulad",
                    audioUrl = alafasyUrl(6224)
                ),
                QuranAyah(
                    numberInSurah = 4,
                    globalNumber = 6225,
                    arabicText = "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                    banglaTranslation = "এবং তাঁর সমতুল্য কেউ নেই।",
                    transliteration = "Wa lam yakun lahu kufuwan ahad",
                    audioUrl = alafasyUrl(6225)
                )
            )
        ),
        QuranSurahDetail(
            summary = QuranSurahSummary(
                number = 113,
                arabicName = "سُورَةُ ٱلْفَلَقِ",
                englishName = "Al-Falaq",
                englishTranslation = "The Daybreak",
                banglaName = "সূরা আল-ফালাক",
                ayahCount = 5,
                revelationType = "Meccan"
            ),
            isOfflineVerified = true,
            ayahs = listOf(
                QuranAyah(
                    numberInSurah = 1,
                    globalNumber = 6226,
                    arabicText = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
                    banglaTranslation = "বলুন, আমি আশ্রয় গ্রহণ করছি প্রভাতের পালনকর্তার।",
                    transliteration = "Qul a'udhu bi-rabbil-falaq",
                    audioUrl = alafasyUrl(6226)
                ),
                QuranAyah(
                    numberInSurah = 2,
                    globalNumber = 6227,
                    arabicText = "مِن شَرِّ مَا خَلَقَ",
                    banglaTranslation = "তিনি যা সৃষ্টি করেছেন, তার অনিষ্ট থেকে।",
                    transliteration = "Min sharri ma khalaq",
                    audioUrl = alafasyUrl(6227)
                ),
                QuranAyah(
                    numberInSurah = 3,
                    globalNumber = 6228,
                    arabicText = "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
                    banglaTranslation = "অন্ধকার রাত্রির অনিষ্ট থেকে, যখন তা গভীর হয়।",
                    transliteration = "Wa min sharri ghasiqin idha waqab",
                    audioUrl = alafasyUrl(6228)
                ),
                QuranAyah(
                    numberInSurah = 4,
                    globalNumber = 6229,
                    arabicText = "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
                    banglaTranslation = "গ্রন্থিতে ফুঁৎকারদানকারিণীদের অনিষ্ট থেকে।",
                    transliteration = "Wa min sharrin-naffathati fil-'uqad",
                    audioUrl = alafasyUrl(6229)
                ),
                QuranAyah(
                    numberInSurah = 5,
                    globalNumber = 6230,
                    arabicText = "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                    banglaTranslation = "এবং হিংসুকের অনিষ্ট থেকে, যখন সে হিংসা করে।",
                    transliteration = "Wa min sharri hasidin idha hasad",
                    audioUrl = alafasyUrl(6230)
                )
            )
        ),
        QuranSurahDetail(
            summary = QuranSurahSummary(
                number = 114,
                arabicName = "سُورَةُ ٱلنَّاسِ",
                englishName = "An-Nas",
                englishTranslation = "Mankind",
                banglaName = "সূরা আন-নাস",
                ayahCount = 6,
                revelationType = "Meccan"
            ),
            isOfflineVerified = true,
            ayahs = listOf(
                QuranAyah(
                    numberInSurah = 1,
                    globalNumber = 6231,
                    arabicText = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
                    banglaTranslation = "বলুন, আমি আশ্রয় গ্রহণ করছি মানুষের পালনকর্তার।",
                    transliteration = "Qul a'udhu bi-rabbin-nas",
                    audioUrl = alafasyUrl(6231)
                ),
                QuranAyah(
                    numberInSurah = 2,
                    globalNumber = 6232,
                    arabicText = "مَلِكِ النَّاسِ",
                    banglaTranslation = "মানুষের অধিপতির।",
                    transliteration = "Malikin-nas",
                    audioUrl = alafasyUrl(6232)
                ),
                QuranAyah(
                    numberInSurah = 3,
                    globalNumber = 6233,
                    arabicText = "إِلَٰهِ النَّاسِ",
                    banglaTranslation = "মানুষের মাবুদের।",
                    transliteration = "Ilahin-nas",
                    audioUrl = alafasyUrl(6233)
                ),
                QuranAyah(
                    numberInSurah = 4,
                    globalNumber = 6234,
                    arabicText = "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
                    banglaTranslation = "তার অনিষ্ট থেকে, যে কুমন্ত্রণা দেয় ও আত্মগোপন করে।",
                    transliteration = "Min sharril-waswasil-khannas",
                    audioUrl = alafasyUrl(6234)
                ),
                QuranAyah(
                    numberInSurah = 5,
                    globalNumber = 6235,
                    arabicText = "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
                    banglaTranslation = "যে কুমন্ত্রণা দেয় মানুষের অন্তরে।",
                    transliteration = "Alladhi yuwaswisu fi sudurin-nas",
                    audioUrl = alafasyUrl(6235)
                ),
                QuranAyah(
                    numberInSurah = 6,
                    globalNumber = 6236,
                    arabicText = "مِنَ الْجِنَّةِ وَالنَّاسِ",
                    banglaTranslation = "জ্বিনের মধ্য থেকে অথবা মানুষের মধ্য থেকে।",
                    transliteration = "Minal-jinnati wan-nas",
                    audioUrl = alafasyUrl(6236)
                )
            )
        )
    )

    val dailyDuas: List<DuaEntry> = listOf(
        DuaEntry(
            id = "dua_rabba_atina",
            category = DuaCategory.SALAH,
            titleEn = "Goodness in Both Worlds",
            titleBn = "দুনিয়া ও আখিরাতের কল্যাণ",
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar.",
            translationBn = "হে আমাদের পালনকর্তা! আমাদেরকে দুনিয়াতে কল্যাণ দান করুন এবং আখিরাতেও কল্যাণ দান করুন এবং আমাদেরকে জাহান্নামের আযাব থেকে রক্ষা করুন।",
            translationEn = "Our Lord, give us in this world good and in the Hereafter good and protect us from the punishment of the Fire.",
            reference = "Surah Al-Baqarah 2:201"
        ),
        DuaEntry(
            id = "dua_after_salah",
            category = DuaCategory.SALAH,
            titleEn = "Help in Remembrance & Worship",
            titleBn = "জিকির, শুকরিয়া ও উত্তম ইবাদতের দোয়া",
            arabic = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            transliteration = "Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik.",
            translationBn = "হে আল্লাহ! আপনার জিকির করতে, আপনার শুকরিয়া আদায় করতে এবং সুন্দরভাবে আপনার ইবাদত করতে আমাকে সাহায্য করুন।",
            translationEn = "O Allah, help me to remember You, to thank You, and to worship You in the best of manners.",
            reference = "Abu Dawud 1522"
        ),
        DuaEntry(
            id = "dua_morning_protection",
            category = DuaCategory.MORNING_EVENING,
            titleEn = "Morning & Evening Protection",
            titleBn = "সকাল ও সন্ধ্যার নিরাপত্তার দোয়া",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa huwas-Sami'ul-'Alim.",
            translationBn = "আল্লাহর নামে (শুরু করছি), যাঁর নামের বরকতে আসমান ও জমিনের কোনো কিছুই ক্ষতি করতে পারে না। তিনি সর্বশ্রোতা ও সর্বজ্ঞ।",
            translationEn = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, All-Knowing.",
            reference = "Tirmidhi 3388"
        ),
        DuaEntry(
            id = "dua_leaving_home",
            category = DuaCategory.DAILY_LIFE,
            titleEn = "When Leaving Home",
            titleBn = "ঘর থেকে বের হওয়ার দোয়া",
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi tawakkaltu 'alallahi wa la hawla wa la quwwata illa billah.",
            translationBn = "আল্লাহর নামে বের হচ্ছি, আল্লাহর ওপর ভরসা করছি; আল্লাহর সাহায্য ছাড়া কোনো শক্তি ও সামর্থ্য নেই।",
            translationEn = "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.",
            reference = "Abu Dawud 5095"
        ),
        DuaEntry(
            id = "dua_entering_mosque",
            category = DuaCategory.SALAH,
            titleEn = "Entering the Mosque",
            titleBn = "মসজিদে প্রবেশের দোয়া",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allahummaf-tah li abwaba rahmatik.",
            translationBn = "হে আল্লাহ! আমার জন্য আপনার রহমতের দরজাসমূহ খুলে দিন।",
            translationEn = "O Allah, open for me the doors of Your mercy.",
            reference = "Sahih Muslim 713"
        ),
        DuaEntry(
            id = "dua_before_sleep",
            category = DuaCategory.DAILY_LIFE,
            titleEn = "Before Sleeping",
            titleBn = "ঘুমানোর আগের দোয়া",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya.",
            translationBn = "হে আল্লাহ! আপনারই নামে আমি মৃত্যুবরণ করি (ঘুমাই) এবং আপনারই নামে জীবিত হই (জাগ্রত হই)।",
            translationEn = "In Your name, O Allah, I die and I live.",
            reference = "Sahih al-Bukhari 6324"
        ),
        DuaEntry(
            id = "dua_sayyidul_istighfar",
            category = DuaCategory.FORGIVENESS,
            titleEn = "Seeking Forgiveness (Yunus AS)",
            titleBn = "ক্ষমা ও মুক্তির দোয়া",
            arabic = "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin.",
            translationBn = "আপনি ব্যতীত কোনো উপাস্য নেই, আপনি পবিত্র; নিশ্চয় আমি জালিমদের অন্তর্ভুক্ত হয়ে গিয়েছিলাম।",
            translationEn = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            reference = "Surah Al-Anbiya 21:87"
        ),
        DuaEntry(
            id = "dua_iftar",
            category = DuaCategory.DAILY_LIFE,
            titleEn = "Upon Breaking the Fast (Iftar)",
            titleBn = "ইফতারের দোয়া",
            arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliteration = "Dhahabaz-zama'u wabtallatil-'uruqu wa thabatal-ajru in sha'Allah.",
            translationBn = "তৃষ্ণা দূর হয়েছে, শিরা-উপশিরা সিক্ত হয়েছে এবং ইনশাআল্লাহ সাওয়াব সাব্যস্ত হয়েছে।",
            translationEn = "The thirst has gone, the veins are quenched, and the reward is confirmed, if Allah wills.",
            reference = "Abu Dawud 2357"
        )
    )
}
