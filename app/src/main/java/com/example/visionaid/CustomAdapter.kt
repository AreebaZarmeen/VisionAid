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
    private val list: List<String>,
    val flag: Boolean,
    private val onContactCheckedChange: (String, Boolean) -> Unit,
    private val onContactClick: (String) -> Unit
) : ArrayAdapter<String>(context, 0, list) {

    // Maintain a set to keep track of checked items
    private val checkedItems = mutableSetOf<String>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view: View
        if(flag)
        {
            view = convertView ?: LayoutInflater.from(context).inflate(R.layout.slection_list, parent, false)

            val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
            val nameTextView = view.findViewById<TextView>(R.id.name)

            val contactName = list[position]
            nameTextView.text = contactName

            // Set checkbox state based on checkedItems set
            checkBox.isChecked = checkedItems.contains(contactName)

            // Set listener to handle checkbox state changes
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkedItems.add(contactName)
                    nameTextView.background = ContextCompat.getDrawable(context, R.color.selected_contact)
                    checkBox.background = ContextCompat.getDrawable(context, R.color.selected_contact)

                } else {
                    checkedItems.remove(contactName)
                    nameTextView.background = ContextCompat.getDrawable(context, R.color.contact)
                    checkBox.background = ContextCompat.getDrawable(context, R.color.contact)
                }
                onContactCheckedChange(contactName, isChecked)
            }
            nameTextView.setOnClickListener {
                onContactClick(contactName) // Trigger callback to handle call
            }
        }
        else
        {
            view = convertView ?: LayoutInflater.from(context).inflate(R.layout.slection_list, parent, false)
        }

        return view
    }
}
