@file:Suppress("UnstableApiUsage")

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask

apply<MavenPublishPlugin>()
apply<MavenPlugin>()
apply<BintrayPlugin>()

val siteUrl: String by extra
val vcsUrl: String by extra

configure<PublishingExtension> {
    publications.filterIsInstance<MavenPublication>().forEach { publication ->
        publication.pom {
            url.set(siteUrl)

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }

            scm {
                url.set(vcsUrl)
            }
        }
    }
}

afterEvaluate {
    tasks {
        maybeCreate("generateAllPomFiles").apply {
            dependsOn(filter { it.name.startsWith("generatePomFile") && it.name.endsWith("Publication") })
        }
        maybeCreate("generateAllMetadataFiles").apply {
            dependsOn(filter { it.name.startsWith("generateMetadataFile") && it.name.endsWith("Publication") })
        }
    }
}

(extra["bintray_user"] as? String)?.also { bintrayUser ->
    (extra["bintray_apikey"] as? String)?.also { bintrayApiKey ->
        val projectName = name
        configure<BintrayExtension> {
            user = bintrayUser
            key = bintrayApiKey
            override = true
            publish = true
            pkg.apply {
                userOrg = "ekito"
                repo = "koin"
                name = projectName
                desc = description
                setLicenses("Apache-2.0")
                vcsUrl = vcsUrl
                websiteUrl = siteUrl
            }
        }
        tasks.withType(BintrayUploadTask::class) {
            doLast {
                publicationUploads.forEach {
                    logger.lifecycle("upload artifact: ${it.path}")
                }
            }
        }
    }
}