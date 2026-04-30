package com.project.integrationsdk.simtracker


object OperatorConfig {

    // ──────────────────────────────────────────────
    // POC – India
    // ──────────────────────────────────────────────

    /**
     * MCC+MNC codes that belong to Jio (primary operator for POC).
     * 404 = MCC India, 486 = MNC Reliance Jio
     * 405 = MCC India (alternate), 857, 861 = Jio alternate MNCs
     */
    val PRIMARY_OPERATOR_MCCMNC_POC = setOf(
        "40486",  // Reliance Jio – main
        "405857", // Jio alternate
        "405861"  // Jio alternate
    )

    val PRIMARY_OPERATOR_NAMES_POC = setOf(
        "jio", "reliance jio", "reliance", "jio 4g"
    )

    /**
     * Jio competitor MCC+MNC codes in India.
     * These are treated as "relevant secondary SIM" operators.
     */
    val COMPETITOR_MCCMNC_POC = setOf(
        // Airtel
        "40410", "40416", "40445", "40449", "40470",
        "40492", "40493", "40494", "40496", "40501",
        "405799",
        // Vi (Vodafone-Idea)
        "40420", "40427", "40430", "40443", "40460",
        "40484", "40487", "40488", "40489", "40495",
        "405845", "405846",
        // BSNL
        "40471", "40474", "40475", "40476", "40477",
        "40734", "40735",
        // MTNL (Delhi/Mumbai)
        "40456", "40468"
    )

    val COMPETITOR_NAMES_POC = setOf(
        "airtel", "bharti airtel",
        "vi", "vodafone", "idea", "vodafone in", "vi india",
        "bsnl",
        "mtnl"
    )

    // ──────────────────────────────────────────────
    // PRODUCTION – Ooredoo markets
    // ──────────────────────────────────────────────

    /**
     * Ooredoo group MCC+MNC codes across all markets.
     * Qatar / Kuwait / Tunisia / Algeria / Myanmar / Maldives / Palestin / Iraq / Oman
     */
    val PRIMARY_OPERATOR_MCCMNC_PROD = setOf(
        // Ooredoo Qatar
        "42701",
        // Ooredoo Kuwait
        "41902",
        // Ooredoo Tunisia
        "60502",
        // Ooredoo Algeria
        "60303",
        // Ooredoo Myanmar
        "41409",
        // Ooredoo Maldives
        "47202",
        // Ooredoo Palestine
        "42505",
        // Asiacell Iraq (Ooredoo-backed)
        "41805",
        // Indosat Ooredoo Indonesia
        "51001", "51021",
        // Ooredoo Oman
        "42203"
    )

    val PRIMARY_OPERATOR_NAMES_PROD = setOf(
        "ooredoo", "asiacell", "indosat", "indosat ooredoo",
        "wataniya", "nedjma"
    )

    /**
     * Competitor MCC+MNC codes in Ooredoo markets.
     */
    val COMPETITOR_MCCMNC_PROD = setOf(
        // Zain Qatar
        "42702",
        // Zain Kuwait
        "41904",
        // Zain Saudi Arabia
        "42004",
        // Zain Iraq
        "41801",
        // Zain Bahrain
        "42604",
        // Zain Jordan
        "41603",
        // e& / Etisalat UAE
        "42402",
        // du UAE
        "42403",
        // STC Saudi Arabia
        "42001",
        // STC Kuwait
        "41903",
        // STC Bahrain
        "42601",
        // Vodafone Qatar
        "42703",
        // Viva Kuwait
        "41905",
        // Viva Bahrain
        "42605",
        // Mobily Saudi Arabia
        "42003",
        // Omantel Oman
        "42202",
        // Tele2 / other Gulf operators
        "42601"
    )

    val COMPETITOR_NAMES_PROD = setOf(
        "zain", "etisalat", "e&", "du", "stc", "saudi telecom",
        "vodafone qatar", "viva", "mobily", "omantel",
        "batelco", "ooredoo competitor"
    )

    // ──────────────────────────────────────────────
    // Active config — flip this flag to switch modes
    // ──────────────────────────────────────────────
    const val IS_POC_MODE = true   // true = India/Jio  |  false = Ooredoo production

    val activePrimaryMccMnc get() = if (IS_POC_MODE) PRIMARY_OPERATOR_MCCMNC_POC else PRIMARY_OPERATOR_MCCMNC_PROD
    val activePrimaryNames   get() = if (IS_POC_MODE) PRIMARY_OPERATOR_NAMES_POC  else PRIMARY_OPERATOR_NAMES_PROD
    val activeCompetitorMccMnc get() = if (IS_POC_MODE) COMPETITOR_MCCMNC_POC     else COMPETITOR_MCCMNC_PROD
    val activeCompetitorNames  get() = if (IS_POC_MODE) COMPETITOR_NAMES_POC      else COMPETITOR_NAMES_PROD
}