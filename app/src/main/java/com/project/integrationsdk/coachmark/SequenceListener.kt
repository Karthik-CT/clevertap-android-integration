package com.project.integrationsdk.coachmark

interface SequenceListener{
    fun onNextItem(coachMark : CoachMarkOverlay, coachMarkSequence : CoachMarkSequence){
        coachMarkSequence.setNextView()
    }
}