package com.example.data.service

import com.example.data.model.BibleVerse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BibleVerseService {

    private val verses = listOf(
        BibleVerse(
            reference = "Proverbs 3:5-6",
            text = "Trust in the Lord with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.",
            translation = "NIV",
            theme = "Guidance & Trust"
        ),
        BibleVerse(
            reference = "Philippians 4:6-7",
            text = "Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God. And the peace of God, which transcends all understanding, will guard your hearts and your minds.",
            translation = "NIV",
            theme = "Peace & Courage"
        ),
        BibleVerse(
            reference = "Psalm 23:1-3",
            text = "The Lord is my shepherd, I lack nothing. He makes me lie down in green pastures, he leads me beside quiet waters, he refreshes my soul.",
            translation = "NIV",
            theme = "Comfort & Care"
        ),
        BibleVerse(
            reference = "Isaiah 40:31",
            text = "Those who hope in the Lord will renew their strength. They will soar on wings like eagles; they will run and not grow weary, they will walk and not be faint.",
            translation = "NIV",
            theme = "Strength & Renewal"
        ),
        BibleVerse(
            reference = "Jeremiah 29:11",
            text = "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you, plans to give you hope and a future.",
            translation = "NIV",
            theme = "Hope & Purpose"
        ),
        BibleVerse(
            reference = "Matthew 6:33-34",
            text = "Seek first his kingdom and his righteousness, and all these things will be given to you as well. Therefore do not worry about tomorrow, for tomorrow will worry about itself.",
            translation = "NIV",
            theme = "Peace of Mind"
        ),
        BibleVerse(
            reference = "Joshua 1:9",
            text = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go.",
            translation = "NIV",
            theme = "Courage & Faith"
        ),
        BibleVerse(
            reference = "Romans 8:28",
            text = "And we know that in all things God works for the good of those who love him, who have been called according to his purpose.",
            translation = "NIV",
            theme = "Confidence & Purpose"
        ),
        BibleVerse(
            reference = "Psalm 118:24",
            text = "This is the day that the Lord has made; let us rejoice and be glad in it.",
            translation = "ESV",
            theme = "Joy & Gratitude"
        ),
        BibleVerse(
            reference = "John 14:27",
            text = "Peace I leave with you; my peace I give you. I do not give to you as the world gives. Do not let your hearts be troubled and do not be afraid.",
            translation = "NIV",
            theme = "Inner Peace"
        ),
        BibleVerse(
            reference = "2 Timothy 1:7",
            text = "For the Spirit God gave us does not make us timid, but gives us power, love and self-discipline.",
            translation = "NIV",
            theme = "Strength & Love"
        ),
        BibleVerse(
            reference = "Colossians 3:12-14",
            text = "Clothe yourselves with compassion, kindness, humility, gentleness and patience. Bear with each other and forgive one another. And over all these virtues put on love.",
            translation = "NIV",
            theme = "Kindness & Compassion"
        ),
        BibleVerse(
            reference = "Psalm 46:1-2",
            text = "God is our refuge and strength, an ever-present help in trouble. Therefore we will not fear, though the earth give way and the mountains fall into the heart of the sea.",
            translation = "NIV",
            theme = "Refuge & Security"
        ),
        BibleVerse(
            reference = "Lamentations 3:22-23",
            text = "Because of the Lord's great love we are not consumed, for his compassions never fail. They are new every morning; great is your faithfulness.",
            translation = "NIV",
            theme = "New Beginnings"
        ),
        BibleVerse(
            reference = "Micah 6:8",
            text = "He has shown you, O mortal, what is good. And what does the Lord require of you? To act justly and to love mercy and to walk humbly with your God.",
            translation = "NIV",
            theme = "Justice & Humility"
        ),
        BibleVerse(
            reference = "Philippians 4:13",
            text = "I can do all this through him who gives me strength.",
            translation = "NIV",
            theme = "Perseverance"
        ),
        BibleVerse(
            reference = "Psalm 121:1-2",
            text = "I lift up my eyes to the mountains—where does my help come from? My help comes from the Lord, the Maker of heaven and earth.",
            translation = "NIV",
            theme = "Protection & Help"
        ),
        BibleVerse(
            reference = "Romans 12:2",
            text = "Do not conform to the pattern of this world, but be transformed by the renewing of your mind. Then you will be able to test and approve what God's will is.",
            translation = "NIV",
            theme = "Mindfulness & Clarity"
        ),
        BibleVerse(
            reference = "Galatians 5:22-23",
            text = "The fruit of the Spirit is love, joy, peace, forbearance, kindness, goodness, faithfulness, gentleness and self-control.",
            translation = "NIV",
            theme = "Character & Harmony"
        ),
        BibleVerse(
            reference = "Matthew 11:28-29",
            text = "Come to me, all you who are weary and burdened, and I will give you rest. Take my yoke upon you and learn from me, for I am gentle and humble in heart, and you will find rest for your souls.",
            translation = "NIV",
            theme = "Rest & Solace"
        ),
        BibleVerse(
            reference = "1 Peter 5:7",
            text = "Cast all your anxiety on him because he cares for you.",
            translation = "NIV",
            theme = "Care & Relief"
        ),
        BibleVerse(
            reference = "Psalm 37:4-5",
            text = "Take delight in the Lord, and he will give you the desires of your heart. Commit your way to the Lord; trust in him and he will do this.",
            translation = "NIV",
            theme = "Trust & Joy"
        ),
        BibleVerse(
            reference = "Hebrews 11:1",
            text = "Now faith is confidence in what we hope for and assurance about what we do not see.",
            translation = "NIV",
            theme = "Faith & Conviction"
        ),
        BibleVerse(
            reference = "Romans 15:13",
            text = "May the God of hope fill you with all joy and peace as you trust in him, so that you may overflow with hope by the power of the Holy Spirit.",
            translation = "NIV",
            theme = "Hope & Joy"
        ),
        BibleVerse(
            reference = "Proverbs 16:3",
            text = "Commit to the Lord whatever you do, and he will establish your plans.",
            translation = "NIV",
            theme = "Endeavor & Wisdom"
        ),
        BibleVerse(
            reference = "Psalm 103:1-2",
            text = "Praise the Lord, my soul; all my inmost being, praise his holy name. Praise the Lord, my soul, and forget not all his benefits.",
            translation = "NIV",
            theme = "Thanksgiving"
        ),
        BibleVerse(
            reference = "Isaiah 41:10",
            text = "So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you; I will uphold you with my righteous right hand.",
            translation = "NIV",
            theme = "Assurance & Comfort"
        ),
        BibleVerse(
            reference = "Ephesians 4:32",
            text = "Be kind and compassionate to one another, forgiving each other, just as in Christ God forgave you.",
            translation = "NIV",
            theme = "Forgiveness & Love"
        ),
        BibleVerse(
            reference = "1 Corinthians 13:4-7",
            text = "Love is patient, love is kind. It does not envy, it does not boast, it is not proud. It always protects, always trusts, always hopes, always perseveres.",
            translation = "NIV",
            theme = "Unconditional Love"
        ),
        BibleVerse(
            reference = "Psalm 19:14",
            text = "May these words of my mouth and this meditation of my heart be pleasing in your sight, Lord, my Rock and my Redeemer.",
            translation = "NIV",
            theme = "Daily Meditation"
        )
    )

    fun getDailyVerse(date: Date = Date()): BibleVerse {
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        val formattedDate = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(date)

        val index = (dayOfYear + year) % verses.size
        val chosen = verses[index]
        return chosen.copy(dateFormatted = formattedDate)
    }
}
