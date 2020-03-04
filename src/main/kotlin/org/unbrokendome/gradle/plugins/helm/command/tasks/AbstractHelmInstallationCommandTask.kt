package org.unbrokendome.gradle.plugins.helm.command.tasks

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.unbrokendome.gradle.plugins.helm.command.HelmExecSpec
import org.unbrokendome.gradle.plugins.helm.command.valuesOptions
import org.unbrokendome.gradle.plugins.helm.util.mapProperty
import org.unbrokendome.gradle.plugins.helm.util.property
import java.io.File
import java.net.URI


abstract class AbstractHelmInstallationCommandTask : AbstractHelmServerCommandTask() {

    /**
     * Release name.
     */
    @get:Input
    val releaseName: Property<String> =
        project.objects.property()


    /**
     * The chart to be installed. This can be any of the forms accepted by the Helm CLI.
     *
     * - chart reference: e.g. `stable/mariadb`
     * - path to a packaged chart
     * - path to an unpacked chart directory
     * - absolute URL: e.g. `https://example.com/charts/nginx-1.2.3.tgz`
     * - simple chart reference, e.g. `mariadb` (you must also set the [repository] property in this case)
     */
    @get:Input
    val chart: Property<String> =
        project.objects.property()


    /**
     * Sets the chart to be installed. The value can be any of the forms accepted by the Helm CLI.
     *
     * This is a convenience method that can be used instead of setting the [chart] property directly.
     *
     * The following argument types are accepted:
     *
     * - A chart reference (`String`): e.g. `stable/mariadb`.
     * - A path to a packaged chart (`String`, [File], [RegularFile])
     * - A path to an unpacked chart directory (`String`, [File], [Directory])
     * - An absolute URL (`String`, [URI]): e.g. `https://example.com/charts/nginx-1.2.3.tgz`
     * - A simple chart reference (`String`), e.g. `mariadb`.
     *   Note that you must also set the [repository] property in this case.
     * - a [Provider] of any of the above.
     *
     */
    fun from(chart: Any) {
        if (chart is Provider<*>) {
            this.chart.set(chart.map { it.toString() })
        } else {
            this.chart.set(chart.toString())
        }
    }


    /**
     * If `true`, roll back changes on failure.
     *
     * Corresponds to the `--atomic` Helm CLI parameter.
     */
    @get:Internal
    val atomic: Property<Boolean> =
        project.objects.property()


    /**
     * Verify certificates of HTTPS-enabled servers using this CA bundle.
     *
     * Corresponds to the `--ca-file` CLI parameter.
     */
    @get:Internal
    val caFile: RegularFileProperty =
        project.objects.fileProperty()


    /**
     * Identify HTTPS client using this SSL certificate file.
     *
     * Corresponds to the `--cert-file` CLI parameter.
     */
    @get:Internal
    val certFile: RegularFileProperty =
        project.objects.fileProperty()


    /**
     * If `true`, use development versions, too. Equivalent to version `>0.0.0-0`.
     * If [version] is set, this is ignored.
     *
     * Corresponds to the `--devel` CLI parameter.
     */
    @get:Input
    val devel: Property<Boolean> =
        project.objects.property<Boolean>()
            .convention(false)


    /**
     * If `true`, simulate an install.
     *
     * Corresponds to the `--dry-run` CLI parameter.
     */
    @get:Internal
    val dryRun: Property<Boolean> =
        project.objects.property()


    /**
     * Identify HTTPS client using this SSL key file.
     *
     * Corresponds to the `--key-file` CLI parameter.
     */
    @get:Internal
    val keyFile: RegularFileProperty =
        project.objects.fileProperty()


    /**
     * If `true`, prevent hooks from running during the operation.
     *
     * Corresponds to the `--no-hooks` CLI parameter.
     */
    @get:Internal
    val noHooks: Property<Boolean> =
        project.objects.property()


    /**
     * Chart repository password where to locate the requested chart.
     *
     * Corresponds to the `--password` CLI parameter.
     */
    @get:Internal
    val password: Property<String> =
        project.objects.property()


    /**
     * Chart repository URL where to locate the requested chart.
     *
     * Corresponds to the `--repo` Helm CLI parameter.
     *
     * Use this when the [chart] property contains only a simple chart reference, without a symbolic repository name.
     */
    @get:[Input Optional]
    val repository: Property<URI> =
        project.objects.property()


    /**
     * Chart repository username where to locate the requested chart.
     *
     * Corresponds to the `--username` CLI parameter.
     */
    @get:Internal
    val username: Property<String> =
        project.objects.property()


    /**
     * Values to be used for the release.
     */
    @get:Input
    val values: MapProperty<String, Any> =
        project.objects.mapProperty()


    /**
     * A collection of YAML files containing values for this release.
     */
    @get:InputFiles
    val valueFiles: ConfigurableFileCollection =
        project.objects.fileCollection()


    /**
     * If `true`, verify the package before installing it.
     *
     * Corresponds to the `--verify` CLI parameter.
     */
    @get:Internal
    val verify: Property<Boolean> =
        project.objects.property()


    /**
     * Specify the exact chart version to install. If this is not specified, the latest version is installed.
     *
     * Corresponds to the `--version` Helm CLI parameter.
     */
    @get:Internal
    val version: Property<String> =
        project.objects.property()


    /**
     * If `true`, will wait until all Pods, PVCs, Services, and minimum number of Pods of a Deployment are in a ready
     * state before marking the release as successful. It will wait for as along as [remoteTimeout].
     */
    @get:Internal
    val wait: Property<Boolean> =
        project.objects.property()


    override fun modifyHelmExecSpec(exec: HelmExecSpec) {
        super.modifyHelmExecSpec(exec)
        exec.run {
            args(releaseName, chart)

            flag("--atomic", atomic)
            option("--ca-file", caFile)
            option("--cert-file", certFile)
            flag("--devel", devel)
            flag("--dry-run", dryRun)
            option("--key-file", keyFile)
            flag("--no-hooks", noHooks)
            option("--password", password)
            option("--repo", repository)
            option("--username", username)
            flag("--verify", verify)
            option("--version", version)
            flag("--wait", wait)

            valuesOptions(values, valueFiles)
        }
    }
}
