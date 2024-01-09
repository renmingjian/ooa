package com.aai.iqa.node.type

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.aai.core.processManager.model.OSPCountryType
import com.aai.core.utils.setContentFont
import com.aai.core.utils.textWithKey
import com.aai.iqa.R

class DocTypeAdapter(private val context: Context, private val list: MutableList<OSPCountryType>) :
    BaseAdapter() {

    private val map = mutableMapOf(
        "PASSPORT" to R.drawable.doc_type_passport,
        "ID_CARD" to R.drawable.doc_type_id_card,
        "DRIVING_LICENSE" to R.drawable.doc_type_driving_license
    )

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): OSPCountryType = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val retView: View
        if (convertView == null) {
            retView = LayoutInflater.from(context)
                .inflate(R.layout.doc_type_item, parent, false)
            holder = ViewHolder(retView)
            retView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            retView = convertView
        }

        val item = getItem(position)
        setContentFont(holder.typeName, textWithKey(item.labelKey, ""))
        var drawableRes = map[item.type]
        if (drawableRes == null) {
            drawableRes = map["ID_CARD"]
        }
        holder.ivType.setImageResource(drawableRes!!)
        return retView
    }

    private class ViewHolder(view: View) {
        val ivType: ImageView = view.findViewById(R.id.ivType)
        val typeName: TextView = view.findViewById(R.id.typeName)
    }
}