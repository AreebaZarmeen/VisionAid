package com.example.visionaid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat

class CustomAdapter(
    context: Context,
    private val contacts: List<String>,
    private val showCheckBox: Boolean,
    private val onContactCheckedChange: (String, Boolean) -> Unit,
    private val onContactClick: (String) -> Unit
) : ArrayAdapter<String>(context, R.layout.contact_list, contacts) {
    // Implementation of your adapter methods


    private val checkedItems = mutableSetOf<String>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.slection_list, parent, false)

        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val nameTextView = view.findViewById<TextView>(R.id.name)

        val contactName = contacts[position]
        nameTextView.text = contactName
        checkBox.isChecked = checkedItems.contains(contactName)

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedItems.add(contactName)
                nameTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_contact))
            } else {
                checkedItems.remove(contactName)
                nameTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.contact))
            }
            onContactCheckedChange(contactName, isChecked)
        }

        nameTextView.setOnClickListener {
            onContactClick(contactName)
        }

        return view
    }
}
