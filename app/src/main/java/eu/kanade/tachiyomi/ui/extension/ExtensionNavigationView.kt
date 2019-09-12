package eu.kanade.tachiyomi.ui.extension

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.preference.getOrDefault
import eu.kanade.tachiyomi.util.LocaleHelper
import eu.kanade.tachiyomi.util.inflate
import eu.kanade.tachiyomi.widget.SimpleNavigationView
import kotlinx.android.synthetic.main.extension_drawer_content.view.*
import uy.kohesive.injekt.injectLazy
import kotlin.collections.HashMap


/**
 * The navigation view shown in a drawer with the different options to show the library.
 */
class ExtensionNavigationView @JvmOverloads constructor(context: Context,
                                                        attrs: AttributeSet? = null,
                                                        defStyleAttr: Int = 0)
    : SimpleNavigationView(context, attrs, defStyleAttr) {

    /**
     * Preferences helper.
     */
    val preferences: PreferencesHelper by injectLazy()

    var filterChanged = {}

    var locale: MutableMap<String, Boolean> = HashMap()

    /**
     * Adapter instance.
     */
    val adapter: FlexibleAdapter<IFlexible<*>> = FlexibleAdapter<IFlexible<*>>(null)
            .setDisplayHeadersAtStartUp(true)
            .setStickyHeaders(true)

    init {
        recycler.adapter = adapter
        recycler.setHasFixedSize(true)
        val view = inflate(R.layout.extension_drawer_content)
        ((view as ViewGroup).getChildAt(1) as ViewGroup).addView(recycler)
        addView(view)
        title.text = context.getString(R.string.action_filter)
    }

    fun parseLocales(extensions: List<ExtensionItem>): MutableMap<String, Boolean> {
        adapter.clear()
        locale.clear()
        for (item in extensions) {
            val name = item.extension.lang.toString()

            if (!locale.containsKey(name)) {
                val checkBox = LangCheckBox(name)
                val entry = CheckboxItem(checkBox)
                locale[name] = checkBox.state

                adapter.addItem(entry)
            }
        }

        return locale
    }

    inner class LangCheckBox(val name: String) {
        private var _state: Boolean = preferences.extensionLangPreference(name).getOrDefault()
        var state: Boolean
                get() = _state
                set(value) {
                    preferences.extensionLangPreference(name).set(value)
                    _state = preferences.extensionLangPreference(name).getOrDefault()
                    locale[name] = _state
                }
    }

    inner class CheckboxItem(val filter: LangCheckBox) : AbstractFlexibleItem<CheckboxItem.Holder>() {

        override fun getLayoutRes(): Int {
            return R.layout.navigation_view_checkbox
        }

        override fun createViewHolder(view: View, adapter: FlexibleAdapter<*>): Holder {
            return Holder(view, adapter)
        }

        override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: Holder, position: Int, payloads: List<Any?>?) {
            val view = holder.check
            view.text = LocaleHelper.getDisplayName(filter.name, context)
            view.isChecked = filter.state
            holder.itemView.setOnClickListener {
                view.toggle()
                filter.state = view.isChecked
                filterChanged()
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return filter == (other as CheckboxItem).filter
        }

        override fun hashCode(): Int {
            return filter.hashCode()
        }

        inner class Holder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {
            val check: CheckBox = itemView.findViewById(R.id.nav_view_item)
        }
    }
}