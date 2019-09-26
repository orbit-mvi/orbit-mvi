#!/usr/bin/env bash
./gradlew clean
./gradlew :orbit:assemble
./gradlew :orbit:publishLibPublicationToInternalRepository
./gradlew :orbit-android:assembleRelease
./gradlew :orbit-android:publishLibPublicationToInternalRepository
./gradlew :datalayer:assemble
./gradlew :datalayer:publishLibPublicationToInternalRepository
