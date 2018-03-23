# Releasing

 1. Change the version in `gradle.properties`.
 2. Update the `CHANGELOG.md` for the impending release.
 3. Update the `README.md` with the new version.
 4. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 5. `./gradlew clean build install bintrayUpload`.
 6. `git tag -a X.Y.X -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 7. `git push && git push --tags`

If step 5 fails, drop the Bintray repo, fix the problem, commit, and start again at step 5.




