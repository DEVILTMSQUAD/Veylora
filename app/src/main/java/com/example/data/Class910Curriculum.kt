package com.example.data

import com.example.data.model.ChapterItem
import com.example.data.model.SubjectItem

object Class910Curriculum {

    const val GROUP_SCIENCE = "বিজ্ঞান (Science)"
    const val GROUP_COMMERCE = "ব্যবসায় শিক্ষা (Commerce)"
    const val GROUP_ARTS = "মানবিক (Arts)"

    val groupList = listOf(GROUP_SCIENCE, GROUP_COMMERCE, GROUP_ARTS)

    // বাংলা ১ম পত্র
    val bangla1st = SubjectItem(
        name = "বাংলা সাহিত্য (১ম পত্র)",
        icon = "📜",
        colorHex = "#14B8A6",
        chapters = listOf(
            // গদ্যাংশ (১০টি)
            ChapterItem(name = "শুভা – রবীন্দ্রনাথ ঠাকুর", done = false),
            ChapterItem(name = "বই পড়া – প্রমথ চৌধুরী", done = false),
            ChapterItem(name = "অভাগীর স্বর্গ – শরৎচন্দ্র চট্টোপাধ্যায়", done = false),
            ChapterItem(name = "পল্লীসাহিত্য – মুহম্মদ শহীদুল্লাহ্", done = false),
            ChapterItem(name = "আম-আঁটির ভেঁপু – বিভূতিভূষণ বন্দ্যোপাধ্যায়", done = false),
            ChapterItem(name = "মানুষ মুহম্মদ (স.) – মোহাম্মদ ওয়াজেদ আলী", done = false),
            ChapterItem(name = "নিমগাছ – বনফুল", done = false),
            ChapterItem(name = "শিক্ষা ও মনুষ্যত্ব – মোতাহের হোসেন চৌধুরী", done = false),
            ChapterItem(name = "প্রবাস বন্ধু – সৈয়দ মুজতবা আলী", done = false),
            ChapterItem(name = "একাত্তরের দিনগুলি – জাহানারা ইমাম", done = false),
            // পদ্যাংশ (১০টি)
            ChapterItem(name = "বঙ্গবাণী – আবদুল হাকিম", done = false),
            ChapterItem(name = "কপোতাক্ষ নদ – মাইকেল মধুসূদন দত্ত", done = false),
            ChapterItem(name = "জীবন-সঙ্গীত – হেমচন্দ্র বন্দ্যোপাধ্যায়", done = false),
            ChapterItem(name = "জুতা-আবিষ্কার – রবীন্দ্রনাথ ঠাকুর", done = false),
            ChapterItem(name = "মানুষ – কাজী নজরুল ইসলাম", done = false),
            ChapterItem(name = "পল্লীজননী – জসীমউদ্দীন", done = false),
            ChapterItem(name = "আশা – সিকান্দার আবু জাফর", done = false),
            ChapterItem(name = "আমি কোনো আগন্তুক নই – আহসান হাবীব", done = false),
            ChapterItem(name = "রানার – সুকান্ত ভট্টাচার্য", done = false),
            ChapterItem(name = "তোমাকে পাওয়ার জন্যে, হে স্বাধীনতা – শামসুর রাহমান", done = false),
            // সহপাঠ
            ChapterItem(name = "উপন্যাস: কাকতাড়ুয়া – সেলিনা হোসেন", done = false),
            ChapterItem(name = "নাটক: বহিপীর – সৈয়দ ওয়ালীউল্লাহ্", done = false)
        )
    )

    // বাংলা ২য় পত্র
    val bangla2nd = SubjectItem(
        name = "বাংলা ব্যাকরণ ও নির্মিতি (২য় পত্র)",
        icon = "📖",
        colorHex = "#0EA5E9",
        chapters = listOf(
            ChapterItem(name = "পরিচ্ছেদ ১: ভাষা ও বাংলা ভাষা", done = false),
            ChapterItem(name = "পরিচ্ছেদ ২: ধ্বনি ও বর্ণ", done = false),
            ChapterItem(name = "পরিচ্ছেদ ৩: সন্ধি", done = false),
            ChapterItem(name = "পরিচ্ছেদ ৪: ণ-ত্ব ও ষ-ত্ব বিধান", done = false),
            ChapterItem(name = "পরিচ্ছেদ ৫: শব্দ ও পদ প্রকরণ", done = false),
            ChapterItem(name = "পরিচ্ছেদ ৬: সমাস", done = false),
            ChapterItem(name = "পরিচ্ছেদ ৭: উপসর্গ ও প্রত্যয়", done = false),
            ChapterItem(name = "পরিচ্ছেদ ৮: বাক্য প্রকরণ ও পরিবর্তন", done = false),
            ChapterItem(name = "পরিচ্ছেদ ৯: বিরামচিহ্ন", done = false),
            ChapterItem(name = "পরিচ্ছেদ ১০: বাগধারা ও প্রবাদ-প্রবচন", done = false),
            ChapterItem(name = "নির্মিতি: ভাব-সম্প্রসারণ ও সারাংশ", done = false),
            ChapterItem(name = "নির্মিতি: প্রতিবেদন ও অনুচ্ছেদ রচনা", done = false)
        )
    )

    // English
    val english = SubjectItem(
        name = "English (1st & 2nd Paper)",
        icon = "🔤",
        colorHex = "#6366F1",
        chapters = listOf(
            ChapterItem(name = "Unit 1: Good Citizens", done = false),
            ChapterItem(name = "Unit 2: Pastimes", done = false),
            ChapterItem(name = "Unit 3: Events and Festivals", done = false),
            ChapterItem(name = "Unit 4: Are We Aware?", done = false),
            ChapterItem(name = "Unit 5: Nature and Environment", done = false),
            ChapterItem(name = "Unit 7: People Who Stand Out", done = false),
            ChapterItem(name = "Unit 10: Dreams", done = false),
            ChapterItem(name = "Grammar: Right Form of Verbs", done = false),
            ChapterItem(name = "Grammar: Changing Sentences (Voice & Degree)", done = false),
            ChapterItem(name = "Grammar: Tag Questions & Connectors", done = false),
            ChapterItem(name = "Writing: Paragraph & Story Writing", done = false),
            ChapterItem(name = "Writing: Formal Letter & Email", done = false)
        )
    )

    // সাধারণ গণিত (১৭টি অধ্যায়)
    val generalMath = SubjectItem(
        name = "সাধারণ গণিত (General Math)",
        icon = "📐",
        colorHex = "#F59E0B",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: বাস্তব সংখ্যা", done = false),
            ChapterItem(name = "অধ্যায় ২: সেট ও ফাংশন", done = false),
            ChapterItem(name = "অধ্যায় ৩: বীজগাণিতিক রাশি", done = false),
            ChapterItem(name = "অধ্যায় ৪: সূচক ও লগারিদম", done = false),
            ChapterItem(name = "অধ্যায় ৫: এক চলকবিশিষ্ট সমীকরণ", done = false),
            ChapterItem(name = "অধ্যায় ৬: রেখা, কোণ ও ত্রিভুজ", done = false),
            ChapterItem(name = "অধ্যায় ৭: ব্যবহারিক জ্যামিতি", done = false),
            ChapterItem(name = "অধ্যায় ৮: বৃত্ত", done = false),
            ChapterItem(name = "অধ্যায় ৯: ত্রিকোণমিতিক অনুপাত", done = false),
            ChapterItem(name = "অধ্যায় ১০: দূরত্ব ও উচ্চতা", done = false),
            ChapterItem(name = "অধ্যায় ১১: বীজগাণিতিক অনুপাত ও সমানুপাত", done = false),
            ChapterItem(name = "অধ্যায় ১২: দুই চলকবিশিষ্ট সরল সহসমীকরণ", done = false),
            ChapterItem(name = "অধ্যায় ১৩: সসীম ধারা", done = false),
            ChapterItem(name = "অধ্যায় ১৪: অনুপাত, সদৃশতা ও প্রতিসমতা", done = false),
            ChapterItem(name = "অধ্যায় ১৫: ক্ষেত্রফল সম্পর্কিত উপপাদ্য ও সম্পাদ্য", done = false),
            ChapterItem(name = "অধ্যায় ১৬: পরিমিতি", done = false),
            ChapterItem(name = "অধ্যায় ১৭: পরিসংখ্যান", done = false)
        )
    )

    // তথ্য ও যোগাযোগ প্রযুক্তি (ICT - ৬টি অধ্যায়)
    val ict = SubjectItem(
        name = "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)",
        icon = "💻",
        colorHex = "#06B6D4",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: তথ্য ও যোগাযোগ প্রযুক্তি এবং আমাদের বাংলাদেশ", done = false),
            ChapterItem(name = "অধ্যায় ২: কম্পিউটার ও কম্পিউটার ব্যবহারকারীর নিরাপত্তা", done = false),
            ChapterItem(name = "অধ্যায় ৩: আমার শিক্ষায় ইন্টারনেট", done = false),
            ChapterItem(name = "অধ্যায় ৪: আমার লেখালেখি ও হিসাব", done = false),
            ChapterItem(name = "অধ্যায় ৫: মাল্টিমিডিয়া ও গ্রাফিক্স", done = false),
            ChapterItem(name = "অধ্যায় ৬: ডেটাবেজ-এর ব্যবহার", done = false)
        )
    )

    // বাংলাদেশ ও বিশ্বপরিচয় (BGS - ১৬টি অধ্যায়)
    val bgs = SubjectItem(
        name = "বাংলাদেশ ও বিশ্বপরিচয় (BGS)",
        icon = "🌏",
        colorHex = "#10B981",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: পূর্ব বাংলার আন্দোলন ও জাতীয়তাবাদের উত্থান (১৯৪৭-১৯৭০)", done = false),
            ChapterItem(name = "অধ্যায় ২: স্বাধীন বাংলাদেশ", done = false),
            ChapterItem(name = "অধ্যায় ৩: সৌরজগৎ ও ভূমণ্ডল", done = false),
            ChapterItem(name = "অধ্যায় ৪: বাংলাদেশের ভূপ্রকৃতি ও জলবায়ু", done = false),
            ChapterItem(name = "অধ্যায় ৫: বাংলাদেশের নদ-নদী ও প্রাকৃতিক সম্পদ", done = false),
            ChapterItem(name = "অধ্যায় ৬: রাষ্ট্র, নাগরিকতা ও আইন", done = false),
            ChapterItem(name = "অধ্যায় ৭: বাংলাদেশ সরকারের অঙ্গসমূহ ও প্রশাসন ব্যবস্থা", done = false),
            ChapterItem(name = "অধ্যায় ৮: বাংলাদেশের রাজনৈতিক দল ও নির্বাচন", done = false),
            ChapterItem(name = "অধ্যায় ৯: জাতিসংঘের সাথে বাংলাদেশের সম্পর্ক", done = false),
            ChapterItem(name = "অধ্যায় ১০: জাতীয় সম্পদ ও অর্থনৈতিক ব্যবস্থা", done = false),
            ChapterItem(name = "অধ্যায় ১১: অর্থনৈতিক নির্দেশকসমূহ ও বাংলাদেশের অর্থনীতি", done = false),
            ChapterItem(name = "অধ্যায় ১২: বাংলাদেশ সরকারের অর্থ ও ব্যাংক ব্যবস্থা", done = false),
            ChapterItem(name = "অধ্যায় ১৩: বাংলাদেশের পরিবার কাঠামো ও সামাজিকীকরণ", done = false),
            ChapterItem(name = "অধ্যায় ১৪: বাংলাদেশের সামাজিক পরিবর্তন", done = false),
            ChapterItem(name = "অধ্যায় ১৫: বাংলাদেশের সামাজিক সমস্যা ও এর প্রতিকার", done = false),
            ChapterItem(name = "অধ্যায় ১৬: বাংলাদেশের সামাজিক পরিবর্তন ও উন্নয়ন", done = false)
        )
    )

    // ইসলাম ও নৈতিক শিক্ষা
    val islam = SubjectItem(
        name = "ইসলাম ও নৈতিক শিক্ষা",
        icon = "🕌",
        colorHex = "#059669",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: আকাইদ ও নৈতিক জীবন", done = false),
            ChapterItem(name = "অধ্যায় ২: শরীয়তের উৎস (কুরআন ও হাদিস)", done = false),
            ChapterItem(name = "অধ্যায় ৩: ইবাদত", done = false),
            ChapterItem(name = "অধ্যায় ৪: আখলাক বা চরিত্র", done = false),
            ChapterItem(name = "অধ্যায় ৫: আদর্শ জীবনচরিত", done = false)
        )
    )

    // ----------------------------------------------------
    // বিজ্ঞান বিভাগ (Science Group)
    // ----------------------------------------------------
    val physics = SubjectItem(
        name = "পদার্থবিজ্ঞান (Physics)",
        icon = "⚡",
        colorHex = "#3B82F6",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: ভৌত রাশি ও পরিমাপ", done = false),
            ChapterItem(name = "অধ্যায় ২: গতি", done = false),
            ChapterItem(name = "অধ্যায় ৩: বল", done = false),
            ChapterItem(name = "অধ্যায় ৪: কাজ, ক্ষমতা ও শক্তি", done = false),
            ChapterItem(name = "অধ্যায় ৫: পদার্থের অবস্থা ও চাপ", done = false),
            ChapterItem(name = "অধ্যায় ৬: বস্তুর উপর তাপের প্রভাব", done = false),
            ChapterItem(name = "অধ্যায় ৭: তরঙ্গ ও শব্দ", done = false),
            ChapterItem(name = "অধ্যায় ৮: আলোর প্রতিফলন", done = false),
            ChapterItem(name = "অধ্যায় ৯: আলোর প্রতিসরণ", done = false),
            ChapterItem(name = "অধ্যায় ১০: স্থির তড়িৎ", done = false),
            ChapterItem(name = "অধ্যায় ১১: চল তড়িৎ", done = false),
            ChapterItem(name = "অধ্যায় ১২: বিদ্যুতের চৌম্বক ক্রিয়া", done = false),
            ChapterItem(name = "অধ্যায় ১৩: আধুনিক পদার্থবিজ্ঞান ও ইলেকট্রনিক্স", done = false),
            ChapterItem(name = "অধ্যায় ১৪: জীবন বাঁচাতে পদার্থবিজ্ঞান", done = false)
        )
    )

    val chemistry = SubjectItem(
        name = "রসায়ন (Chemistry)",
        icon = "🧪",
        colorHex = "#10B981",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: রসায়নের ধারণা", done = false),
            ChapterItem(name = "অধ্যায় ২: পদার্থের অবস্থা", done = false),
            ChapterItem(name = "অধ্যায় ৩: পদার্থের গঠন", done = false),
            ChapterItem(name = "অধ্যায় ৪: পর্যায় সারণি", done = false),
            ChapterItem(name = "অধ্যায় ৫: রাসায়নিক বন্ধন", done = false),
            ChapterItem(name = "অধ্যায় ৬: মোলের ধারণা ও রাসায়নিক গণনা", done = false),
            ChapterItem(name = "অধ্যায় ৭: রাসায়নিক বিক্রিয়া", done = false),
            ChapterItem(name = "অধ্যায় ৮: রসায়ন ও শক্তি", done = false),
            ChapterItem(name = "অধ্যায় ৯: এসিড-ক্ষার সমতা", done = false),
            ChapterItem(name = "অধ্যায় ১০: খনিজ সম্পদ: ধাতু-অধাতু", done = false),
            ChapterItem(name = "অধ্যায় ১১: খনিজ সম্পদ: জীবাশ্ম", done = false),
            ChapterItem(name = "অধ্যায় ১২: আমাদের জীবনে রসায়ন", done = false)
        )
    )

    val biology = SubjectItem(
        name = "জীববিজ্ঞান (Biology)",
        icon = "🧬",
        colorHex = "#8B5CF6",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: জীবন পাঠ", done = false),
            ChapterItem(name = "অধ্যায় ২: জীবকোষ ও টিস্যু", done = false),
            ChapterItem(name = "অধ্যায় ৩: কোষ বিভাজন", done = false),
            ChapterItem(name = "অধ্যায় ৪: জীবনীশক্তি", done = false),
            ChapterItem(name = "অধ্যায় ৫: খাদ্য, পুষ্টি এবং পরিপাক", done = false),
            ChapterItem(name = "অধ্যায় ৬: জীবে পরিবহন", done = false),
            ChapterItem(name = "অধ্যায় ৭: গ্যাসীয় বিনিময়", done = false),
            ChapterItem(name = "অধ্যায় ৮: রেচন প্রক্রিয়া", done = false),
            ChapterItem(name = "অধ্যায় ৯: দৃঢ়তা প্রদান ও চলন", done = false),
            ChapterItem(name = "অধ্যায় ১০: সমন্বয়", done = false),
            ChapterItem(name = "অধ্যায় ১১: জীবের প্রজনন", done = false),
            ChapterItem(name = "অধ্যায় ১২: জীবের বংশগতি ও জৈব অভিব্যক্তি", done = false),
            ChapterItem(name = "অধ্যায় ১৩: জীবের পরিবেশ", done = false),
            ChapterItem(name = "অধ্যায় ১৪: জীবপ্রযুক্তি", done = false)
        )
    )

    val higherMath = SubjectItem(
        name = "উচ্চতর গণিত (Higher Math)",
        icon = "🧮",
        colorHex = "#EC4899",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: সেট ও ফাংশন", done = false),
            ChapterItem(name = "অধ্যায় ২: বীজগাণিতিক রাশি", done = false),
            ChapterItem(name = "অধ্যায় ৩: জ্যামিতি", done = false),
            ChapterItem(name = "অধ্যায় ৪: জ্যামিতিক অঙ্কন", done = false),
            ChapterItem(name = "অধ্যায় ৫: সমীকরণ", done = false),
            ChapterItem(name = "অধ্যায় ৬: অসমতা", done = false),
            ChapterItem(name = "অধ্যায় ৭: অসীম ধারা", done = false),
            ChapterItem(name = "অধ্যায় ৮: ত্রিকোণমিতি", done = false),
            ChapterItem(name = "অধ্যায় ৯: সূচকীয় ও লগারিদমীয় ফাংশন", done = false),
            ChapterItem(name = "অধ্যায় ১০: দ্বিপদী বিস্তৃতি", done = false),
            ChapterItem(name = "অধ্যায় ১১: স্থানাঙ্ক জ্যামিতি", done = false),
            ChapterItem(name = "অধ্যায় ১২: সমতলীয় ভেক্টর", done = false),
            ChapterItem(name = "অধ্যায় ১৩: ঘন জ্যামিতি", done = false),
            ChapterItem(name = "অধ্যায় ১৪: সম্ভাবনা", done = false)
        )
    )

    // ----------------------------------------------------
    // ব্যবসায় শিক্ষা বিভাগ (Commerce Group)
    // ----------------------------------------------------
    val accounting = SubjectItem(
        name = "হিসাববিজ্ঞান (Accounting)",
        icon = "📊",
        colorHex = "#0284C7",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: হিসাববিজ্ঞানের পরিচিতি", done = false),
            ChapterItem(name = "অধ্যায় ২: লেনদেন", done = false),
            ChapterItem(name = "অধ্যায় ৩: দুতরফা দাখিলা পদ্ধতি", done = false),
            ChapterItem(name = "অধ্যায় ৪: মূলধন ও মুনাফাজাতীয় লেনদেন", done = false),
            ChapterItem(name = "অধ্যায় ৫: হিসাব", done = false),
            ChapterItem(name = "অধ্যায় ৬: জাবেদা", done = false),
            ChapterItem(name = "অধ্যায় ৭: খতিয়ান", done = false),
            ChapterItem(name = "অধ্যায় ৮: নগদান বই", done = false),
            ChapterItem(name = "অধ্যায় ৯: রেওয়ামিল", done = false),
            ChapterItem(name = "অধ্যায় ১০: আর্থিক বিবরণী", done = false),
            ChapterItem(name = "অধ্যায় ১১: পণ্যের ক্রয়মূল্য, উৎপাদন ব্যয় ও বিক্রয়মূল্য", done = false),
            ChapterItem(name = "অধ্যায় ১২: পারিবারিক ও আত্মকর্মসংস্থানমূলক প্রকল্পের হিসাব", done = false)
        )
    )

    val businessEnt = SubjectItem(
        name = "ব্যবসায় উদ্যোগ (Business Entrepreneurship)",
        icon = "💼",
        colorHex = "#D97706",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: ব্যবসায় পরিচিতি", done = false),
            ChapterItem(name = "অধ্যায় ২: ব্যবসায় উদ্যোগ ও উদ্যোক্তা", done = false),
            ChapterItem(name = "অধ্যায় ৩: আত্মকর্মসংস্থান", done = false),
            ChapterItem(name = "অধ্যায় ৪: মালিকানার ভিত্তিতে ব্যবসায়", done = false),
            ChapterItem(name = "অধ্যায় ৫: ব্যবসায়ের আইনগত দিক", done = false),
            ChapterItem(name = "অধ্যায় ৬: ব্যবসায় পরিকল্পনার স্বরূপ", done = false),
            ChapterItem(name = "অধ্যায় ৭: বাংলাদেশের শিল্প", done = false),
            ChapterItem(name = "অধ্যায় ৮: ব্যবসায় প্রতিষ্ঠানের ব্যবস্থাপনা", done = false),
            ChapterItem(name = "অধ্যায় ৯: বিপণন বা বাজারজাতকরণ", done = false),
            ChapterItem(name = "অধ্যায় ১০: ব্যবসায় উদ্যোগ উন্নয়নে সহায়ক সেবা", done = false),
            ChapterItem(name = "অধ্যায় ১১: ব্যবসায়ে নৈতিকতা ও সামাজিক দায়বদ্ধতা", done = false)
        )
    )

    val financeBanking = SubjectItem(
        name = "ফিন্যান্স ও ব্যাংকিং (Finance & Banking)",
        icon = "🏦",
        colorHex = "#059669",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: অর্থায়ন ও ব্যবসায় অর্থায়ন", done = false),
            ChapterItem(name = "অধ্যায় ২: অর্থায়নের উৎস", done = false),
            ChapterItem(name = "অধ্যায় ৩: অর্থের সময়মূল্য", done = false),
            ChapterItem(name = "অধ্যায় ৪: ঝুঁকি ও অনিশ্চয়তা", done = false),
            ChapterItem(name = "অধ্যায় ৫: মূলধনি আয়-ব্যয় প্রাক্কলন", done = false),
            ChapterItem(name = "অধ্যায় ৬: মূলধন ব্যয়", done = false),
            ChapterItem(name = "অধ্যায় ৭: শেয়ার, বন্ড ও ডিবেঞ্চার", done = false),
            ChapterItem(name = "অধ্যায় ৮: ব্যাংক ব্যবস্থার প্রাথমিক ধারণা", done = false),
            ChapterItem(name = "অধ্যায় ৯: ব্যাংকিং ব্যবসা ও তার ধরন", done = false),
            ChapterItem(name = "অধ্যায় ১০: বাণিজ্যিক ব্যাংক ও তার পরিচিতি", done = false),
            ChapterItem(name = "অধ্যায় ১১: ব্যাংক আমানত ও সেবা", done = false)
        )
    )

    val generalScience = SubjectItem(
        name = "সাধারণ বিজ্ঞান (General Science)",
        icon = "🔬",
        colorHex = "#2563EB",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: উন্নততর জীবনধারা", done = false),
            ChapterItem(name = "অধ্যায় ২: জীবনের জন্য পানি", done = false),
            ChapterItem(name = "অধ্যায় ৩: হৃদযন্ত্রের যত কথা", done = false),
            ChapterItem(name = "অধ্যায় ৪: নতুন জীবনের সূচনা", done = false),
            ChapterItem(name = "অধ্যায় ৫: দেখতে হলে আলো চাই", done = false),
            ChapterItem(name = "অধ্যায় ৬: পলিমার", done = false),
            ChapterItem(name = "অধ্যায় ৭: অম্ল, ক্ষারক ও লবণের ব্যবহার", done = false),
            ChapterItem(name = "অধ্যায় ৮: আমাদের সম্পদ", done = false),
            ChapterItem(name = "অধ্যায় ৯: দুর্যোগের সাথে বসবাস", done = false),
            ChapterItem(name = "অধ্যায় ১০: এসো বলকে জানি", done = false),
            ChapterItem(name = "অধ্যায় ১১: জীবপ্রযুক্তি", done = false),
            ChapterItem(name = "অধ্যায় ১২: প্রত্যাহিক জীবনে তড়িৎ", done = false),
            ChapterItem(name = "অধ্যায় ১৩: ভারী যন্ত্রপাতি ও যোগাযোগ", done = false),
            ChapterItem(name = "অধ্যায় ১৪: জীবন বাঁচাতে বিজ্ঞান", done = false)
        )
    )

    // ----------------------------------------------------
    // মানবিক বিভাগ (Arts / Humanities Group)
    // ----------------------------------------------------
    val history = SubjectItem(
        name = "বাংলাদেশের ইতিহাস ও বিশ্বসভ্যতা",
        icon = "🏛️",
        colorHex = "#9333EA",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: ইতিহাস পরিচিতি", done = false),
            ChapterItem(name = "অধ্যায় ২: বিশ্বসভ্যতা (মিসর, সিন্ধু, গ্রিক ও রোমান)", done = false),
            ChapterItem(name = "অধ্যায় ৩: প্রাচীন বাংলার জনপদ", done = false),
            ChapterItem(name = "অধ্যায় ৪: প্রাচীন বাংলার রাজনৈতিক ইতিহাস", done = false),
            ChapterItem(name = "অধ্যায় ৫: প্রাচীন বাংলার সামাজিক, অর্থনৈতিক ও সাংস্কৃতিক ইতিহাস", done = false),
            ChapterItem(name = "অধ্যায় ৬: মধ্যযুগের বাংলার রাজনৈতিক ইতিহাস", done = false),
            ChapterItem(name = "অধ্যায় ৭: মধ্যযুগের বাংলার আর্থ-সামাজিক ও সাংস্কৃতিক ইতিহাস", done = false),
            ChapterItem(name = "অধ্যায় ৮: বাংলায় ইংরেজ শাসনের সূচনা", done = false),
            ChapterItem(name = "অধ্যায় ৯: ইংরেজ শাসন বিরোধী আন্দোলন", done = false),
            ChapterItem(name = "অধ্যায় ১০: বাংলায় জাতীয়তাবাদের বিকাশ", done = false),
            ChapterItem(name = "অধ্যায় ১১: পূর্ব বাংলায় ভাষা আন্দোলন ও তৎপরবর্তী ঘটনাবলি", done = false),
            ChapterItem(name = "অধ্যায় ১২: স্বাধিকার আন্দোলন ও ১৯৭১-এর মুক্তিযুদ্ধ", done = false),
            ChapterItem(name = "অধ্যায় ১৩: স্বাধীন ও সার্বভৌম বাংলাদেশের অভ্যুদয়", done = false),
            ChapterItem(name = "অধ্যায় ১৪: বঙ্গবন্ধু শেখ মুজিবুর রহমানের শাসনকাল", done = false),
            ChapterItem(name = "অধ্যায় ১৫: সাম্প্রতিক বাংলাদেশ ও ভবিষ্যৎ রূপরেখা", done = false)
        )
    )

    val geography = SubjectItem(
        name = "ভূগোল ও পরিবেশ (Geography & Env)",
        icon = "🌍",
        colorHex = "#16A34A",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: ভূগোল ও পরিবেশ পরিচিতি", done = false),
            ChapterItem(name = "অধ্যায় ২: মহাবিশ্ব ও আমাদের পৃথিবী", done = false),
            ChapterItem(name = "অধ্যায় ৩: মানচিত্র পঠন ও ব্যবহার", done = false),
            ChapterItem(name = "অধ্যায় ৪: পৃথিবীর অভ্যন্তরীণ ও বাহ্যিক গঠন", done = false),
            ChapterItem(name = "অধ্যায় ৫: বায়ুমণ্ডল", done = false),
            ChapterItem(name = "অধ্যায় ৬: বারিমণ্ডল", done = false),
            ChapterItem(name = "অধ্যায় ৭: জনসংখ্যা ও মানববসতি", done = false),
            ChapterItem(name = "অধ্যায় ৮: মানব বসতি ও নগরায়ণ", done = false),
            ChapterItem(name = "অধ্যায় ৯: পরিবেশের উপাদান ও দূষণ", done = false),
            ChapterItem(name = "অধ্যায় ১০: বাংলাদেশের ভৌগোলিক বিবরণ", done = false),
            ChapterItem(name = "অধ্যায় ১১: বাংলাদেশের সম্পদ ও শিল্প", done = false),
            ChapterItem(name = "অধ্যায় ১২: বাংলাদেশের যোগাযোগ ব্যবস্থা ও বাণিজ্য", done = false),
            ChapterItem(name = "অধ্যায় ১৩: বাংলাদেশের পরিবেশগত বিপর্যয় ও দুর্যোগ", done = false),
            ChapterItem(name = "অধ্যায় ১৪: টেকসই উন্নয়ন ও ভবিষ্যৎ পরিকল্পনা", done = false)
        )
    )

    val civics = SubjectItem(
        name = "পৌরনীতি ও নাগরিকতা (Civics)",
        icon = "⚖️",
        colorHex = "#BE185D",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: পৌরনীতি ও নাগরিকতা", done = false),
            ChapterItem(name = "অধ্যায় ২: নাগরিক ও নাগরিকতা", done = false),
            ChapterItem(name = "অধ্যায় ৩: আইন, স্বাধীনতা ও সাম্য", done = false),
            ChapterItem(name = "অধ্যায় ৪: রাষ্ট্র ও সরকার ব্যবস্থা", done = false),
            ChapterItem(name = "অধ্যায় ৫: সংবিধান", done = false),
            ChapterItem(name = "অধ্যায় ৬: বাংলাদেশের সরকার ব্যবস্থা", done = false),
            ChapterItem(name = "অধ্যায় ৭: গণতন্ত্রে রাজনৈতিক দল ও নির্বাচন", done = false),
            ChapterItem(name = "অধ্যায় ৮: স্থানীয় সরকার ও প্রশাসন", done = false),
            ChapterItem(name = "অধ্যায় ৯: নাগরিক সমস্যা ও আমাদের করণীয়", done = false),
            ChapterItem(name = "অধ্যায় ১০: স্বাধীন বাংলাদেশের অভ্যুদয়ে নাগরিক চেতনা", done = false),
            ChapterItem(name = "অধ্যায় ১১: বাংলাদেশ ও আন্তর্জাতিক সংস্থাসমূহ", done = false)
        )
    )

    val economics = SubjectItem(
        name = "অর্থনীতি (Economics)",
        icon = "📈",
        colorHex = "#CA8A04",
        chapters = listOf(
            ChapterItem(name = "অধ্যায় ১: অর্থনীতির পরিচয়", done = false),
            ChapterItem(name = "অধ্যায় ২: অর্থনীতির গুরুত্বপূর্ণ ধারণাসমূহ", done = false),
            ChapterItem(name = "অধ্যায় ৩: উপযোগ, চাহিদা, জোগান ও ভারসাম্য", done = false),
            ChapterItem(name = "অধ্যায় ৪: উৎপাদন ও সংগঠন", done = false),
            ChapterItem(name = "অধ্যায় ৫: বাজার", done = false),
            ChapterItem(name = "অধ্যায় ৬: জাতীয় আয় ও এর পরিমাপ", done = false),
            ChapterItem(name = "অধ্যায় ৭: অর্থ ও ব্যাংক ব্যবস্থা", done = false),
            ChapterItem(name = "অধ্যায় ৮: বাংলাদেশের অর্থনীতি", done = false),
            ChapterItem(name = "অধ্যায় ৯: বাংলাদেশের গুরুত্বপূর্ণ অর্থনৈতিক খাতসমূহ", done = false),
            ChapterItem(name = "অধ্যায় ১০: অর্থনৈতিক প্রবৃদ্ধি ও উন্নয়ন", done = false)
        )
    )

    /**
     * Get subjects list tailored for a specific study group (Science, Commerce, Arts)
     */
    fun getSubjectsForGroup(groupName: String): List<SubjectItem> {
        val common = listOf(bangla1st, bangla2nd, english, generalMath, ict, islam)
        return when (groupName) {
            GROUP_COMMERCE -> common + listOf(accounting, businessEnt, financeBanking, generalScience, bgs)
            GROUP_ARTS -> common + listOf(history, geography, civics, economics, generalScience)
            else -> common + listOf(physics, chemistry, biology, higherMath, bgs) // Default Science
        }
    }

    /**
     * Get all Class 9-10 syllabus subjects
     */
    fun getClass910Subjects(): List<SubjectItem> {
        return listOf(
            bangla1st,
            bangla2nd,
            english,
            generalMath,
            physics,
            chemistry,
            biology,
            higherMath,
            ict,
            bgs,
            islam,
            accounting,
            businessEnt,
            financeBanking,
            history,
            geography,
            civics,
            economics
        )
    }
}
