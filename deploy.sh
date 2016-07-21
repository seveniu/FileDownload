#/bin/bash
./gradlew clean -Pprofile=stable
./gradlew build -Pprofile=stable
scp build/libs/fileDownload-0.1.0.jar dali:/opt/app/fileDownload
