package org.unbrokendome.gradle.plugins.helm.release.dsl

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.unbrokendome.gradle.plugins.helm.command.ConfigurableHelmInstallationOptions
import org.unbrokendome.gradle.plugins.helm.command.ConfigurableHelmValueOptions
import org.unbrokendome.gradle.plugins.helm.command.HelmInstallationOptionsHolder
import org.unbrokendome.gradle.plugins.helm.command.HelmValueOptionsHolder
import org.unbrokendome.gradle.plugins.helm.release.tags.TagExpression
import javax.inject.Inject


/**
 * Defines a target configuration for installing and uninstalling releases.
 *
 * A release target can specify its own server options to coordinate to a specific remote Kubernetes cluster.
 * Additionally, it can specify values that should be applied to all releases when installing to this target.
 */
interface HelmReleaseTargetObject : Named, ConfigurableHelmInstallationOptions, ConfigurableHelmValueOptions {

    /**
     * Values to be passed directly.
     * These will be applied to _all_ releases when this target is active.
     *
     * Entries in the map will be sent to the CLI using either the `--set-string` option (for strings) or the
     * `--set` option (for all other types).
     */
    override val values: MapProperty<String, Any>


    /**
     * Values read from the contents of files.
     * These will be applied to _all_ releases when this target is active.
     *
     * Corresponds to the `--set-file` CLI option.
     *
     * The values of the map can be of any type that is accepted by [Project.file]. Additionally, when adding a
     * [Provider] that represents an output file of another task, the consuming task will automatically have a task
     * dependency on the producing task.
     *
     * Not to be confused with [valueFiles], which contains a collection of YAML files that supply multiple values.
     */
    override val fileValues: MapProperty<String, Any>


    /**
     * A collection of YAML files containing values.
     * These will be applied to _all_ releases when this target is active.
     *
     * Corresponds to the `--values` CLI option.
     *
     * Not to be confused with [fileValues], which contains entries whose values are the contents of files.
     */
    override val valueFiles: ConfigurableFileCollection


    /**
     * Tag expression to select releases for this target.
     */
    var selectTags: String
}


internal interface HelmReleaseTargetInternal : HelmReleaseTargetObject {

    /**
     * Tag expression to select releases for this target.
     */
    val selectTagsExpression: TagExpression
}


private open class DefaultHelmReleaseTargetObject
@Inject constructor(
    private val name: String,
    private val globalSelectTagsExpression: TagExpression,
    objects: ObjectFactory
) : HelmReleaseTargetObject, HelmReleaseTargetInternal,
    ConfigurableHelmInstallationOptions by HelmInstallationOptionsHolder(objects),
    ConfigurableHelmValueOptions by HelmValueOptionsHolder(objects) {

    private var localSelectTagsExpression: TagExpression =
        TagExpression.alwaysMatch()


    override fun getName(): String =
        name


    final override var selectTags: String = "*"
        set(value) {
            field = value
            localSelectTagsExpression = TagExpression.parse(value)
        }


    override val selectTagsExpression: TagExpression
        get() = localSelectTagsExpression.and(globalSelectTagsExpression)
}


/**
 * Defines a target configuration for installing and uninstalling releases.
 *
 * A release target can specify its own server options to coordinate to a specific remote Kubernetes cluster.
 * Additionally, it can specify values that should be applied to all releases when installing to this target.
 *
 * This interface also exposes [ExtensionAware], so that build scripts can access the `ext` (Groovy) / `extra`
 * (Kotlin) properties of the release.
 */
interface HelmReleaseTarget : HelmReleaseTargetObject, ExtensionAware


private class HelmReleaseTargetDecorator(
        private val delegate: HelmReleaseTargetInternal
) : HelmReleaseTarget, HelmReleaseTargetInternal by delegate, ExtensionAware by delegate as ExtensionAware


internal fun Project.helmReleaseTargetContainer(
    globalSelectTagsExpression: TagExpression
): NamedDomainObjectContainer<HelmReleaseTarget> =
    container(HelmReleaseTarget::class.java) { name ->
        val delegate = objects.newInstance(DefaultHelmReleaseTargetObject::class.java, name, globalSelectTagsExpression)
        HelmReleaseTargetDecorator(delegate)
    }


internal fun HelmReleaseTarget.shouldInclude(release: HelmCoreRelease): Boolean =
    (this as HelmReleaseTargetInternal).selectTagsExpression.matches(release.tags)
