package com.aai.iqa.node.country

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.aai.core.processManager.model.OSPSupportCountry
import com.aai.core.utils.setContentFont
import com.aai.core.utils.textWithKey
import com.aai.iqa.R

class CountryAdapter(
    context: Context,
    private val countries: List<OSPSupportCountry>
) : ArrayAdapter<OSPSupportCountry>(context, 0, countries) {

    var selectedIndex = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent, true)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent, false)
    }

    private fun createItemView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        isDropList: Boolean
    ): View {
        val holder: ViewHolder
        val retView: View

        if (convertView == null) {
            retView = LayoutInflater.from(context)
                .inflate(R.layout.doc_country_spinner_item, parent, false)
            holder = ViewHolder(retView)
            retView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            retView = convertView
        }

        val countryItem = getItem(position)
        val key = "select_country_${countryItem?.countryCode}"
        var countryName = textWithKey(key, key)
        if (countryName.isEmpty()) {
            countryName = countryItem?.label ?: ""
        }
        setContentFont(
            holder.countryName,
            countryName,
            fontWeight = if (selectedIndex == position) "700" else "500"
        )
//        countryItem?.flag?.let { holder.countryFlag.setImageResource(it) }
        if (position == countries.size - 1) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        if (isDropList && position == 0) {
            holder.divider.visibility = View.GONE
        }
        return retView
    }

    private class ViewHolder(view: View) {
        val countryFlag: ImageView = view.findViewById(R.id.ivCountry)
        val countryName: TextView = view.findViewById(R.id.tvCountryName)
        val divider: View = view.findViewById(R.id.divider)
    }
}

