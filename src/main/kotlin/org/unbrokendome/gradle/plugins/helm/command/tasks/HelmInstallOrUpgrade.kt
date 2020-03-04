package org.unbrokendome.gradle.plugins.helm.command.tasks

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.unbrokendome.gradle.plugins.helm.util.property


/**
 * Installs a chart into a remote Kubernetes cluster as a new release, or upgrades an existing release.
 */
open class HelmInstallOrUpgrade : AbstractHelmInstallationCommandTask() {

    /**
     * If `true`, re-use the given release name, even if that name is already used.
     *
     * If this is `true`, the task will perform a `helm install --replace` command. If it is `false` (default), then
     * it will perform a `helm upgrade --install` command instead.
     */
    @get:Internal
    val replace: Property<Boolean> =
        project.objects.property<Boolean>()
            .convention(false)


    /**
     * If `true`, reset the values to the ones built into the chart when upgrading.
     *
     * Corresponds to the `--reset-values` parameter of the `helm upgrade` CLI command.
     *
     * If [replace] is set to `true`, this property will be ignored.
     */
    @get:Internal
    val resetValues: Property<Boolean> =
        project.objects.property()


    /**
     * If `true`, reuse the last release's values, and merge in any new values. If [resetValues] is specified,
     * this is ignored.

     * Corresponds to the `--reuse-values` parameter of the `helm upgrade` CLI command.
     *
     * If [replace] is set to `true`, this property will be ignored.
     */
    @get:Internal
    val reuseValues: Property<Boolean> =
        project.objects.property()


    @TaskAction
    fun installOrUpgrade() {
        if (replace.get()) {
            execHelm("install") {
                flag("--replace")
            }

        } else {
            execHelm("upgrade") {
                flag("--install")
                flag("--reset-values", resetValues)
                flag("--reuse-values", reuseValues)
            }
        }
    }
}
