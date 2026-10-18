package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Class910Curriculum
import com.example.data.VeyloraRepository
import com.example.notifications.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Veylora", appName)
    }

    @Test
    fun `verify Class 9-10 curriculum subjects and chapters`() {
        val subjects = Class910Curriculum.getClass910Subjects()
        assertTrue("Curriculum should have at least 15 subjects", subjects.size >= 15)

        // 1. Bangla 1st Paper
        val bangla = subjects.find { it.name.contains("বাংলা সাহিত্য") }
        assertNotNull("Bangla 1st should exist", bangla)
        assertTrue("Bangla should have at least 22 chapters (10 prose, 10 poetry, novel, drama)", bangla!!.chapters.size >= 22)

        // 2. General Math
        val math = subjects.find { it.name.contains("সাধারণ গণিত") }
        assertNotNull("General Math should exist", math)
        assertEquals("General Math must have 17 chapters", 17, math!!.chapters.size)

        // 3. Physics
        val physics = subjects.find { it.name.contains("পদার্থবিজ্ঞান") }
        assertNotNull("Physics should exist", physics)
        assertEquals("Physics must have 14 chapters", 14, physics!!.chapters.size)

        // 4. ICT
        val ict = subjects.find { it.name.contains("তথ্য ও যোগাযোগ প্রযুক্তি") }
        assertNotNull("ICT should exist", ict)
        assertEquals("ICT must have 6 chapters", 6, ict!!.chapters.size)

        // 5. BGS
        val bgs = subjects.find { it.name.contains("বাংলাদেশ ও বিশ্বপরিচয়") }
        assertNotNull("BGS should exist", bgs)
        assertEquals("BGS must have 16 chapters", 16, bgs!!.chapters.size)
    }

    @Test
    fun `verify study groups for Science, Commerce, and Arts`() {
        val scienceSubs = Class910Curriculum.getSubjectsForGroup(Class910Curriculum.GROUP_SCIENCE)
        val commerceSubs = Class910Curriculum.getSubjectsForGroup(Class910Curriculum.GROUP_COMMERCE)
        val artsSubs = Class910Curriculum.getSubjectsForGroup(Class910Curriculum.GROUP_ARTS)

        assertTrue("Science should have Physics", scienceSubs.any { it.name.contains("পদার্থবিজ্ঞান") })
        assertTrue("Commerce should have Accounting", commerceSubs.any { it.name.contains("হিসাববিজ্ঞান") })
        assertTrue("Arts should have History", artsSubs.any { it.name.contains("ইতিহাস") })
    }

    @Test
    fun `verify NotificationHelper time parser`() {
        val (h1, m1) = NotificationHelper.parseTimeString("08:30 AM")
        assertEquals(8, h1)
        assertEquals(30, m1)

        val (h2, m2) = NotificationHelper.parseTimeString("08:30 PM")
        assertEquals(20, h2)
        assertEquals(30, m2)

        val (h3, m3) = NotificationHelper.parseTimeString("20:45")
        assertEquals(20, h3)
        assertEquals(45, m3)
    }

    @Test
    fun `verify repository profile and group update`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = VeyloraRepository(context)

        repository.updateUserProfile(
            name = "Test Student",
            studentClass = "৯ম-১০ম শ্রেণী / SSC",
            group = Class910Curriculum.GROUP_COMMERCE,
            dailyGoalMinutes = 180
        )

        assertEquals("Test Student", repository.profile.value.name)
        assertEquals(Class910Curriculum.GROUP_COMMERCE, repository.profile.value.group)
        assertTrue("Subjects should contain commerce subjects", repository.subjects.value.any { it.name.contains("হিসাববিজ্ঞান") })
    }

    @Test
    fun `verify previous account data restore from backup`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = VeyloraRepository(context)

        repository.updateUserProfile(
            name = "Rahim Ahmed",
            studentClass = "১০ম শ্রেণী",
            group = Class910Curriculum.GROUP_SCIENCE,
            dailyGoalMinutes = 150
        )
        repository.addTask("Physics Chapter 2 Math Practice", "পদার্থবিজ্ঞান (Physics)", 2, "2026-10-20", "10:00 AM")
        val exported = repository.exportDataJson()

        val newRepo = VeyloraRepository(context)
        val success = newRepo.importDataJson(exported)

        assertTrue("Importing previous account data should succeed", success)
        assertEquals("Rahim Ahmed", newRepo.profile.value.name)
        assertEquals("১০ম শ্রেণী", newRepo.profile.value.studentClass)
        assertEquals(Class910Curriculum.GROUP_SCIENCE, newRepo.profile.value.group)
        assertTrue("Previous tasks must be restored", newRepo.tasks.value.any { it.title.contains("Physics Chapter 2") })
    }
}
