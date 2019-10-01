#!/usr/bin/env bash
./gradlew clean
./gradlew :orbit:assemble
./gradlew :orbit:publishLibPublicationToPublicRepository
./gradlew :orbit-android:assembleRelease
./gradlew :orbit-android:publishLibPublicationToPublicRepository
