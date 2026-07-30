package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NoteTemplate(
    val name: String,
    val description: String,
    val content: String,
    val icon: ImageVector
)

object Templates {
    val all = listOf(
        NoteTemplate(
            name = "Daily Journal",
            description = "Track your daily thoughts, wins, and challenges.",
            icon = Icons.Default.Today,
            content = """
                ## Daily Journal - ${System.currentTimeMillis()}
                
                ### How am I feeling today?
                - 
                
                ### Top 3 Wins
                1. 
                2. 
                3. 
                
                ### Challenges & Learnings
                - 
                
                ### Tomorrow's Focus
                - 
            """.trimIndent()
        ),
        NoteTemplate(
            name = "Meeting Notes",
            description = "Structure your professional meetings and action items.",
            icon = Icons.Default.Groups,
            content = """
                ## Meeting: [Title]
                Date: ${System.currentTimeMillis()}
                Attendees: 
                
                ### Agenda
                - 
                
                ### Key Discussion Points
                - 
                
                ### Action Items
                - [ ] 
                - [ ] 
                
                ### Next Steps
                - 
            """.trimIndent()
        ),
        NoteTemplate(
            name = "Project Kickoff",
            description = "Start new projects with clear goals and requirements.",
            icon = Icons.Default.RocketLaunch,
            content = """
                # Project: [Name] 🚀
                
                ### Objectives
                - 
                
                ### Requirements
                - 
                
                ### Milestones
                - [ ] Phase 1: 
                - [ ] Phase 2: 
                
                ### Resources
                - 
            """.trimIndent()
        ),
        NoteTemplate(
            name = "Code Snippet",
            description = "Standard code block template for developers.",
            icon = Icons.Default.Code,
            content = """
                ### Snippet: [Title]
                
                ```kotlin
                // Write your code here
                ```
                
                **Notes:**
                - 
            """.trimIndent()
        ),
        NoteTemplate(
            name = "Shopping List",
            description = "Quick checklist for your next store run.",
            icon = Icons.Default.ShoppingCart,
            content = """
                ## Shopping List 🛒
                
                - [ ] Milk
                - [ ] Eggs
                - [ ] Bread
                - [ ] 
            """.trimIndent()
        )
    )
}
