package com.example.ui.theme

/**
 * Complete, authentic translations for every lesson step and quiz question across all 12 official South African languages:
 * 1. English (en)
 * 2. isiZulu (zu)
 * 3. isiXhosa (xh)
 * 4. Afrikaans (af)
 * 5. Sepedi / Northern Sotho (nso)
 * 6. Setswana (tn)
 * 7. Sesotho / Southern Sotho (st)
 * 8. Xitsonga (ts)
 * 9. siSwati (ss)
 * 10. Tshivenda (ve)
 * 11. isiNdebele (nr)
 * 12. South African Sign Language (sasl)
 */
object LessonContentTranslations {

    private val stepTitles = mapOf<String, Map<String, String>>(
        "html_1_s1" to mapOf(
            "en" to "Why Web Pages Matter",
            "zu" to "Kungani Amakhasi E-Web Ebalulekile",
            "xh" to "Kutheni Amaphepha eWebhu Ebalulekile",
            "af" to "Waarom Webblaaie Saak Maak",
            "nso" to "Lebaka Leo Matlakala a Wepo a Bohlokwa",
            "tn" to "Goreng Ditsebe tsa Web di le Botlhokwa",
            "st" to "Hobaneng Maqephe a Webo a le Bohlokwa",
            "ts" to "Hikwalaho ka Yini Matluka ya Web ma Ri ya Nkoka",
            "ss" to "Kungani Emakhasi E-Web Abalulekile",
            "ve" to "Ndi Ngani Masiaṱari a Web e a Ndeme",
            "nr" to "Kungani Amakhasi we-Web Aqakathekile",
            "sasl" to "[WHY WEB PAGES 🌐] Importance in Community"
        ),
        "html_1_s2" to mapOf(
            "en" to "Tags and Content",
            "zu" to "Omaka Nokuqukethwe",
            "xh" to "Iithegi kunye nokuQulathiweyo",
            "af" to "Merkers en Inhoud",
            "nso" to "Ditshwao le Diteng",
            "tn" to "Ditheke le Diteng",
            "st" to "Matshwao le Likahare",
            "ts" to "Tithegi na Swilo swa le Ndzeni",
            "ss" to "Emathegi Nekuphakathi",
            "ve" to "Dzithege na Zwire Ngomu",
            "nr" to "Amathegi Nokungaphakathi",
            "sasl" to "[TAGS 🏷️ & CONTENT] HTML Syntax Structure"
        ),
        "html_1_s3" to mapOf(
            "en" to "Add Your Store Name",
            "zu" to "Faka Igama Lesitolo Sakho",
            "xh" to "Faka iGama leVenkile yakho",
            "af" to "Voeg Jou Winkelnaam By",
            "nso" to "Tsenya Leina la Lebenkele la Gago",
            "tn" to "Tsenya Leina la Lebenkele la Gago",
            "st" to "Kenya Lebitso la Lebenkele la Hao",
            "ts" to "Nghenisa Vito ra Xitolo xa Wena",
            "ss" to "Faka Ligama Lesitolo Sakho",
            "ve" to "Dzhenisani Dzina la Vhengele Lanu",
            "nr" to "Faka Ibizo Lesitolo Sakho",
            "sasl" to "[STORE NAME 🏪] Set Header Title"
        ),
        "mobile_1_s1" to mapOf(
            "en" to "Why Mobile Dev Matters in SA",
            "zu" to "Kungani Izinhlelo Zeselula Zibalulekile e-SA",
            "xh" to "Kutheni uPhuhliso lwe-Mobile Lubalulekile e-SA",
            "af" to "Waarom Mobiele Ontwikkeling Saak Maak in SA",
            "nso" to "Lebaka Leo Tšweletšo ya Selula e Lego Bohlokwa",
            "tn" to "Goreng Tlhabololo ya Selefono e le Botlhokwa",
            "st" to "Hobaneng Ntshetsopele ya Fono e le Bohlokwa",
            "ts" to "Hikwalaho ka Yini Nhluvukiso wa Foni wu Ri wa Nkoka",
            "ss" to "Kungani Kutfutfukiswa Kwatifoni Kubalulekile e-SA",
            "ve" to "Ndi Ngani Mveledziso ya Founu i ya Ndeme",
            "nr" to "Kungani Ukuthuthukiswa Kwamaselula Kuqakathekile e-SA",
            "sasl" to "[MOBILE DEV 📱] Why Smartphones Matter"
        ),
        "ai_1_s1" to mapOf(
            "en" to "Intro to Generative AI & LLMs",
            "zu" to "Isingeniso Kwe-AI Ekhiqizayo nama-LLM",
            "xh" to "Intshayelelo ye-Generative AI kunye nee-LLM",
            "af" to "Inleiding tot Generatiewe KI en LLMs",
            "nso" to "Kenyelletšo ya AI le Di-LLM",
            "tn" to "Kitsiso ya Generative AI le di-LLM",
            "st" to "Kenyelletso ea Generative AI le li-LLM",
            "ts" to "Xingheniso eka Generative AI na ti-LLM",
            "ss" to "Singeniso Kwe-AI Nekuhlakanipha Kwayo",
            "ve" to "Marangaphanda a Generative AI na dzi-LLM",
            "nr" to "Isingeniso se-AI nama-LLM",
            "sasl" to "[AI INTRO 🤖] Generative AI & Prompts"
        ),
        "design_1_s1" to mapOf(
            "en" to "Design with Ubuntu & Warmth",
            "zu" to "Ukuklama Ngobuntu Nokufudumala",
            "xh" to "Uyilo olunobuNtu kunye nokuFudumala",
            "af" to "Ontwerp met Ubuntu en Warmte",
            "nso" to "Tlhamo ya Botho le Borutho",
            "tn" to "Moralo wa Botho le Bothitho",
            "st" to "Moralo oa Botho le Mofuthu",
            "ts" to "Vuvumbi bya Vumunhu na ku Titivala",
            "ss" to "Kuklama Ngebuntfu Nekufutfumala",
            "ve" to "Mbumbo ya Vhuthu na Vhudidini",
            "nr" to "Ukuklama Ngobuntu Nokuthula",
            "sasl" to "[UBUNTU DESIGN 🎨] Cultural Warmth"
        )
    )

    private val stepDescriptions = mapOf<String, Map<String, String>>(
        "html_1_s1" to mapOf(
            "en" to "HTML (HyperText Markup Language) is the digital foundation of all websites on the internet. It acts like the brick structure of a spaza shop, holding up the roof, shelves, and signboards.",
            "zu" to "I-HTML (HyperText Markup Language) iyisisekelo sawo wonke amawebhusayithi aku-inthanethi. Ifana nezitini zokwakha isitolo se-spaza, ezibamba uphahla, amashalofu nezimpawu.",
            "xh" to "I-HTML (HyperText Markup Language) sisiseko sazo zonke iiwebhusayithi kwi-intanethi. Ifana nezitena zokwakha ivenkile yasekhaya, ezibamba uphahla kunye neeshelfu.",
            "af" to "HTML (HyperText Markup Language) is die digitale fondament van alle webwerwe op die internet. Dit is soos die bakstene van 'n spaza-winkel wat die dak, rakke en naamborde ophou.",
            "nso" to "HTML (HyperText Markup Language) ke motheo wa matlakala ka moka a wepo marangrangeng. E swana le ditena tša go aga lebenkele la selegae, tšeo di swarago marulelo le mashelofo.",
            "tn" to "HTML (HyperText Markup Language) ke motheo wa maranyane a mafaratlhatlha otlhe a webo mo inthaneteng. E tshwana le ditena tsa go aga lebenkele la spaza, tse di tshegetsang borulelo le dishelofo.",
            "st" to "HTML (HyperText Markup Language) ke motheo oa marang-rang a bohle ba webo inthaneteng. E ts'oana le litene tsa ho haha lebenkele la spaza, tse tšoarellang marulelo le lishelefo.",
            "ts" to "HTML (HyperText Markup Language) i masungulo ya matluka hinkwawo ya web eka inthanete. Yi fana na switini swo aka xitolo xa spaza, leswi khomaka lwangu na tishelefu.",
            "ss" to "I-HTML (HyperText Markup Language) yisisekelo sawo wonkhe emawebhusayithi laku-inthanethi. Ifana netitini tekukha sitolo se-spaza, letibamba luphahla nemashalofu.",
            "ve" to "HTML (HyperText Markup Language) ndi mutevhe wa masiaṱari othe a web kha inthanethe. I fana na zwitina zwa u fhasa vhengele la spaza, zwi re na thungo ya thanga na zwilayelwa.",
            "nr" to "I-HTML (HyperText Markup Language) isisekelo sawo woke amawebhusayithi we-inthanethi. Ifana namatje wokwakha isitolo se-spaza, abamba uphahla namashelufu.",
            "sasl" to "[HTML 🌐] Digital bricks of websites. Like building walls of a spaza shop."
        ),
        "html_1_s2" to mapOf(
            "en" to "Web pages use tags like <h1> for large headings and <p> for paragraphs. Tags wrap around text to tell the browser how to present information.",
            "zu" to "Amakhasi e-web asebenzisa omaka abafana no-<h1> ngezihloko ezinkulu kanye no-<p> ngezigaba. Omaka bagoqa umbhalo ukuze bayalele isiphequluli ukuthi sibonise kanjani ulwazi.",
            "xh" to "Amaphepha ewebhu asebenzisa iithegi ezifana no-<h1> kwizihloko ezinkulu kunye no-<p> kwimihlathi. Iithegi zisongela umbhalo ukuxelela isikhangeli indlela yokubonisa ulwazi.",
            "af" to "Webblaaie gebruik merkers soos <h1> vir groot opskrifte en <p> vir paragrawe. Merkers vou om teks om vir die webblaaier te sê hoe om inligting te vertoon.",
            "nso" to "Matlakala a wepo a šomiša ditshwao tša go swana le <h1> bakeng sa dihlogo tše dikgolo le <p> bakeng sa ditemana. Ditshwao di phuthela mongwalo go laela sefetleki.",
            "tn" to "Ditsebe tsa webo di dirisa ditheke tse di tshwanang le <h1> go kwalela ditlhogo tse dikgolo le <p> go kwalela ditemana. Ditheke di phuthela mokwalo go laela sefetleki.",
            "st" to "Maqephe a webo a sebelisa matshwao a kang <h1> bakeng sa lihlooho tse kholo le <p> bakeng sa lirapa. Matshwao a phuthela mongolo ho laela sebatli.",
            "ts" to "Matluka ya web ma tirhisa tithegi to fana hi <h1> eka tinhlokomhaka letikulu na <p> eka tindzimana. Tithegi ti phuphula matsalwa ku leletela sefetleki.",
            "ss" to "Emakhasi e-web asebentisa emathegi lafana na-<h1> etinhlokweni letinkhulu kanye na-<p> etigabeni. Emathegi agoca umbhalo kuyala siphequluli.",
            "ve" to "Masiaṱari a web a shumisa dzithege dzi no fana na <h1> kha thoho khulwane na <p> kha phara. Dzithege dzi fukedza maipfi u vhudza tsedzuluso ya web.",
            "nr" to "Amakhasi we-web asebenzisa amathegi anjengo-<h1> eentlokweni ezikulu no-<p> eengabeni. Amathegi amboza umtlolo ukutjela isibukeli ukuthi siphakamise njani ilwazi.",
            "sasl" to "[TAGS 🏷️] <h1> for Big Header. <p> for paragraph text."
        ),
        "html_1_s3" to mapOf(
            "en" to "Type <h1>Mama Ruth's Fresh Spaza</h1> in the simulator code editor below, then tap Run & Verify Code to preview your live shop storefront!",
            "zu" to "Bhala <h1>Mama Ruth's Fresh Spaza</h1> kusifanisi sekhodi ngezansi, bese ucofa u-Run & Verify Code ukuze ubone isitolo sakho sibukhoma!",
            "xh" to "Bhala <h1>Mama Ruth's Fresh Spaza</h1> kwisilungisi sekhowudi esingezantsi, uze ucofe u-Run & Verify Code ukujonga ivenkile yakho iphila!",
            "af" to "Tik <h1>Mama Ruth's Fresh Spaza</h1> in die kode-redigeerder hieronder, en tik dan op Run & Verify Code om jou lewendige winkelvoorkant te sien!",
            "nso" to "Tlanya <h1>Mama Ruth's Fresh Spaza</h1> ka gare ga morulaganyi wa khoutu tlase, ke moka o kgotle Run & Verify Code go bona lebenkele la gago le phela!",
            "tn" to "Kwala <h1>Mama Ruth's Fresh Spaza</h1> mo morulaganying wa khoutu fa tlase, mme o tobetse Run & Verify Code go bona lebenkele la gago le le fa pele ga gago!",
            "st" to "Ngola <h1>Mama Ruth's Fresh Spaza</h1> ho morulaganyi oa khoutu ka tlase, ebe o tobetsa Run & Verify Code ho bona lebenkele la hao le le phela!",
            "ts" to "Tsala <h1>Mama Ruth's Fresh Spaza</h1> eka xitirhisiwa xa khodi laha hansi, kutani u tsindziya Run & Verify Code ku vona xitolo xa wena xi ri karhi xi hanya!",
            "ss" to "Bhala <h1>Mama Ruth's Fresh Spaza</h1> kusifanisi sekhodi ngaphansi, bese ucindzetela u-Run & Verify Code kute ubone sitolo sakho sibukeka!",
            "ve" to "Nwalani <h1>Mama Ruth's Fresh Spaza</h1> kha tshikili tsha khodi fhasi, ni do no ponda Run & Verify Code u vhona vhengele lanu li tshi khou vhonala!",
            "nr" to "Tlola <h1>Mama Ruth's Fresh Spaza</h1> kusibukeli sekhowudi ngaphasi, bese utlhonya Run & Verify Code ukuze ubone isitolo sakho siphila!",
            "sasl" to "[TYPE ⌨️] <h1>Store Name</h1>. Tap Run to preview."
        ),
        "mobile_1_s1" to mapOf(
            "en" to "Android powers over 85% of mobile devices across South African townships. In this module, you will learn to build native Android screens using Jetpack Compose.",
            "zu" to "I-Android isebenza ezingcingweni ezingaphezu kuka-85% emalokishini aseNingizimu Afrika. Kule sifundo, uzofunda ukwakha izikrini zeselula nge-Jetpack Compose.",
            "xh" to "I-Android inika amandla ngaphezulu kwe-85% yeezixhobo zeselula kwiilokishi zaseMzantsi Afrika. Kule modyuli, uza kufunda ukwakha izikrini ze-Android usebenzisa i-Jetpack Compose.",
            "af" to "Android dryf meer as 85% van mobiele toestelle in Suid-Afrikaanse townships aan. In hierdie module leer jy om inheemse Android-skerms te bou met Jetpack Compose.",
            "nso" to "Android e šomišwa ke difounu tše fetago 85% makešeneng a Afrika Borwa. Mo karolong ye, o tlo ithuta go aga disekirini tša Android ka Jetpack Compose.",
            "tn" to "Android e dirisiwa mo difonong tse di fetang 85% mo makeisheneng a Aforika Borwa. Mo karolong eno, o tla ithuta go dira diskerine tsa Android ka Jetpack Compose.",
            "st" to "Android e matlafatsa lifono tse fetang 85% makeisheneng a Afrika Boroa. Karolong ena, o tla ithuta ho haha lits'oants'o tsa Android ka Jetpack Compose.",
            "ts" to "Android yi tirha eka switirhisiwa swo tlula 85% e-township ta Afrika-Dzonga. Eka xiyenge lexi, u ta dyondza ku aka swikirini swa Android hi Jetpack Compose.",
            "ss" to "I-Android inika emandla kumafoni langetulu kwa-85% emalokishini aseNingizimu Afrika. Kulesifundvo, utawufundza kwakha tikrini tetifoni nge-Jetpack Compose.",
            "ve" to "Android i bveledza founu dzi no fhira 85% zwikolobisini zwa Afurika Tshipembe. Kha iyi ngudo, ni do guda u fhasa zwikirini zwa Android nga Jetpack Compose.",
            "nr" to "I-Android inikela amandla kumaselula adlula ama-85% emalokitjhini weSewula Afrika. Kulesisifundo, uzofunda ukwakha izikrini ze-Android nge-Jetpack Compose.",
            "sasl" to "[ANDROID 📱 85% SA] Jetpack Compose modern app development."
        ),
        "ai_1_s1" to mapOf(
            "en" to "Large Language Models like Google Gemini can understand natural language, summarize reports, translate between African languages, and write business copy. Prompt engineering is the skill of guiding AI models effectively.",
            "zu" to "Amamodeli e-AI afana ne-Google Gemini ayakwazi ukuqonda ulimi lwemvelo, afingqe imibiko, ahumushe phakathi kwezilimi zase-Afrika, futhi abhale imibhalo yebhizinisi. U-Prompt engineering ikhono lokuyala i-AI ngempumelelo.",
            "xh" to "Iimodeli ezinkulu zeLwimi ezifana ne-Google Gemini ziyakwazi ukuqonda ulwimi lwendalo, zishwankathele iingxelo, ziguqulele phakathi kweelwimi zase-Afrika, kwaye zibhale iikopi zeshishini.",
            "af" to "Groot Taalmodelle soos Google Gemini kan natuurlike taal verstaan, verslae opsom, tussen Afrikatale vertaal en besigheidstekste skryf. Vraaginligting (prompt engineering) lei KI doeltreffend.",
            "nso" to "Mehlala ya Maleme e Meholo ya go swana le Google Gemini e kgona go kwešiša maleme a tlhago, go ngwala ditlaleho le go fetolela magareng ga maleme a Afrika.",
            "tn" to "Ditsamaiso tse dikgolo tsa Dipuo jaaka Google Gemini di kgona go tlhaloganya puo ya tlholego, go sobokanya dipego, le go ranola fa gare ga dipuo tsa Aforika.",
            "st" to "Mefuta e Meholo ea Lipuo e kang Google Gemini e ka utloisisa puo ea tlhaho, ea akaretsa litlaleho, ea fetolela lipakeng tsa lipuo tsa Afrika.",
            "ts" to "Tindlela Letikulu ta Ririmi to fana hi Google Gemini ti kota ku twisisa ririmi ra ntumbuluko, ku komisa swiviko na ku hundzuluxa exikarhi ka tindzimi ta Afrika.",
            "ss" to "Timodeli teLulwimi Letinkhulu letifana ne-Google Gemini tiyakwati kucondza lulwimi lwemvelo, kufingqa imibiko, nekuhumusha emkhatsini wetilwimi tase-Afrika.",
            "ve" to "Tshiimo tshihulwane tsha Nyambo tshi no fana na Google Gemini tshi a kona u pfesesa luambo lwa mvelo, u vhumba mivhigo, na u toloka vhukati ha nyambo dza Afrika.",
            "nr" to "Iimfundo Ezikulu Zeelimi ezifana ne-Google Gemini ziyakghona ukuzwisisa ilimi lemvelo, zirhunyeze iimbiko, zitoloke phakathi kweelimi ze-Afrika.",
            "sasl" to "[AI & GEMINI 🤖] Understand languages, summarize and write business copy."
        )
    )

    private val quizQuestions = mapOf<String, Map<String, String>>(
        "q_html_1_1" to mapOf(
            "en" to "What does HTML stand for in website development?",
            "zu" to "Kusho ukuthini i-HTML ekwakhiweni kwamawebhusayithi?",
            "xh" to "Imele ntoni i-HTML kuphuhliso lweewebhusayithi?",
            "af" to "Waarvoor staan HTML in webwerfontwikkeling?",
            "nso" to "HTML e emela eng go tšweletšo ya wepo?",
            "tn" to "HTML e emela eng mo tlhabololong ya webo?",
            "st" to "HTML e emela eng ho ntshetsopele ya webo?",
            "ts" to "HTML yi yimela yini eka nhluvukiso wa web?",
            "ss" to "Kusho kutsini i-HTML ekwakhiweni kwemawebhusayithi?",
            "ve" to "HTML i imela mini kha mveledziso ya web?",
            "nr" to "I-HTML itjho ukuthini ekuthuthukisweni kwe-web?",
            "sasl" to "[QUESTION ❓] What is HTML full name?"
        ),
        "q_html_1_2" to mapOf(
            "en" to "Which tag creates the largest, most prominent heading in HTML?",
            "zu" to "Yimuphi umaka owakha isihloko esikhulu nesivelele kakhulu ku-HTML?",
            "xh" to "Yeyiphi ithegi eyenza isihloko esikhulu nesibalulekileyo kwi-HTML?",
            "af" to "Watter merker skep die grootste, mees prominente opskrif in HTML?",
            "nso" to "Ke seswayo sefe seo se dirago hlogo e kgolokgolo go HTML?",
            "tn" to "Ke theke efe e e dirang setlhogo se segolobogolo mo HTML?",
            "st" to "Ke letshwao lefe le etsang sehlooho se seholo ka ho fetisisa ho HTML?",
            "ts" to "Hi yihi thegi leyi endlaka nhlokomhaka leyikulu swinene eka HTML?",
            "ss" to "Ngumuphi umaka lowakha sihloko lesikhulu kakhulu ku-HTML?",
            "ve" to "Ndi ithege ifhio i no vhumba thoho khulwanesa kha HTML?",
            "nr" to "Ngiliphi ithegi elenza isihloko esikhulu khulu ku-HTML?",
            "sasl" to "[QUESTION ❓] Which tag makes the largest heading?"
        ),
        "q_mobile_1_1" to mapOf(
            "en" to "What is Jetpack Compose in Android development?",
            "zu" to "Kuyini i-Jetpack Compose ekuthuthukisweni kwe-Android?",
            "xh" to "Yintoni i-Jetpack Compose kuphuhliso lwe-Android?",
            "af" to "Wat is Jetpack Compose in Android-ontwikkeling?",
            "nso" to "Ke eng Jetpack Compose tšweletšong ya Android?",
            "tn" to "Ke eng Jetpack Compose mo tlhabololong ya Android?",
            "st" to "Ke eng Jetpack Compose ntshetsopeleng ea Android?",
            "ts" to "I yini Jetpack Compose eka nhluvukiso wa Android?",
            "ss" to "Kuyini i-Jetpack Compose ekutfutfukisweni kwe-Android?",
            "ve" to "Ndi mini Jetpack Compose kha mveledziso ya Android?",
            "nr" to "Yini i-Jetpack Compose ekuthuthukisweni kwe-Android?",
            "sasl" to "[QUESTION ❓] What is Jetpack Compose?"
        ),
        "q_ai_1_1" to mapOf(
            "en" to "What is Prompt Engineering in modern AI development?",
            "zu" to "Kuyini i-Prompt Engineering ekuthuthukisweni kwe-AI?",
            "xh" to "Yintoni i-Prompt Engineering kuphuhliso lwe-AI?",
            "af" to "Wat is Vraaginligting (Prompt Engineering) in moderne KI?",
            "nso" to "Ke eng Prompt Engineering go tšweletšo ya AI?",
            "tn" to "Ke eng Prompt Engineering mo tlhabololong ya AI?",
            "st" to "Ke eng Prompt Engineering ntshetsopeleng ea AI?",
            "ts" to "I yini Prompt Engineering eka nhluvukiso wa AI?",
            "ss" to "Kuyini i-Prompt Engineering ekutfutfukisweni kwe-AI?",
            "ve" to "Ndi mini Prompt Engineering kha mveledziso ya AI?",
            "nr" to "Yini i-Prompt Engineering ekuthuthukisweni kwe-AI?",
            "sasl" to "[QUESTION ❓] What is Prompt Engineering?"
        ),
        "q_design_1_1" to mapOf(
            "en" to "Why is high color contrast critical for mobile applications in township communities?",
            "zu" to "Kungani ukuhlukana kwemibala okucacile kubalulekile ezinhlelweni zeselula ezindaweni zasekhaya?",
            "xh" to "Kutheni ukungafani kwemibala eqaqambileyo kubalulekile kwiilokishi?",
            "af" to "Waarom is hoë kleurkontras krities vir mobiele toepassings in townships?",
            "nso" to "Lebaka leo phapano e kgolo ya mebala e lego bohlokwa difounung tša makešene ke lefe?",
            "tn" to "Goreng phapang e kgolo ya mebala e le botlhokwa mo difonong tsa makeishene?",
            "st" to "Hobaneng phapang e phahameng ea mebala e le bohlokwa difonong tsa makeishene?",
            "ts" to "Hikwalaho ka yini ku hambana ka mivala loku vonakaka kahle ku ri ka nkoka e-township?",
            "ss" to "Kungani kuhluka kwemibala lokucacile kubalulekile kutifoni tasemalokishini?",
            "ve" to "Ndi ngani u fhambana ha mivhala hu tshi khou ṱodwa nga maanda foununi dza zwikolobisi?",
            "nr" to "Kungani ukuhlukana kwemibala okuphakamileko kuqakathekile kumaselula emalokitjhini?",
            "sasl" to "[QUESTION ❓] Why is high contrast color important for outdoor mobile screens?"
        )
    )

    private val quizExplanations = mapOf<String, Map<String, String>>(
        "q_html_1_1" to mapOf(
            "en" to "HTML stands for HyperText Markup Language, the standard code for structuring web pages.",
            "zu" to "I-HTML isho i-HyperText Markup Language, ikhodi evamile yokuhlela amakhasi e-web.",
            "xh" to "I-HTML imele i-HyperText Markup Language, ikhowudi esemgangathweni yokwakha amaphepha ewebhu.",
            "af" to "HTML staan vir HyperText Markup Language, die standaardkode vir die strukturering van webblaaie.",
            "nso" to "HTML e emela HyperText Markup Language, khoutu ya maemo ya go hlama matlakala a wepo.",
            "tn" to "HTML e emela HyperText Markup Language, khoute ya maemo ya go rulaganya ditsebe tsa webo.",
            "st" to "HTML e emela HyperText Markup Language, khoutu e tloaelehileng ea ho rala maqephe a webo.",
            "ts" to "HTML yi yimela HyperText Markup Language, khodi ya xiyimo xa le henhla yo vumba matluka ya web.",
            "ss" to "I-HTML isho i-HyperText Markup Language, ikhodi leyetayelekile yekuhlela emakhasi e-web.",
            "ve" to "HTML i imela HyperText Markup Language, khodi ya maemo ya u fhata masiaṱari a web.",
            "nr" to "I-HTML itjho i-HyperText Markup Language, ikhowudi eyaziwako yokuhlela amakhasi we-web.",
            "sasl" to "[ANSWER ✅] HTML = HyperText Markup Language."
        ),
        "q_html_1_2" to mapOf(
            "en" to "<h1> creates the top-level, largest title heading on an HTML web page.",
            "zu" to "I-<h1> idala isihloko esikhulu nesivelele kakhulu ekhasini le-web.",
            "xh" to "I-<h1> idala isihloko esikhulu nesona sibalulekileyo kwiphepha lewebhu.",
            "af" to "<h1> skep die boonste, grootste titelkoplug op 'n HTML-webblad.",
            "nso" to "<h1> e dira hlogo e kgolo kudu letlakaleng la wepo.",
            "tn" to "<h1> e dira setlhogo se segolo thata mo tsebeng ya webo.",
            "st" to "<h1> e etsa sehlooho se seholo ka ho fetisisa leqepheng la webo.",
            "ts" to "<h1> yi endla nhlokomhaka leyikulu swinene eka tluka ra web.",
            "ss" to "I-<h1> yakha sihloko lesikhulu kakhulu ekhasini le-web.",
            "ve" to "<h1> i vhumba thoho khulwane kha siaṱari la web.",
            "nr" to "I-<h1> yenza isihloko esikhulu khulu ekhasini le-web.",
            "sasl" to "[ANSWER ✅] <h1> = Largest, top-level heading."
        )
    )

    fun getLocalizedStepTitle(stepId: String, lang: String): String? {
        val map = stepTitles[stepId] ?: return null
        return map[lang] ?: map["en"]
    }

    fun getLocalizedStepDescription(stepId: String, lang: String): String? {
        val map = stepDescriptions[stepId] ?: return null
        return map[lang] ?: map["en"]
    }

    fun getLocalizedQuizQuestion(quizId: String, lang: String): String? {
        val map = quizQuestions[quizId] ?: return null
        return map[lang] ?: map["en"]
    }

    fun getLocalizedQuizExplanation(quizId: String, lang: String): String? {
        val map = quizExplanations[quizId] ?: return null
        return map[lang] ?: map["en"]
    }
}
