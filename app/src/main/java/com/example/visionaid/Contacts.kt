package com.example.visionaid

import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Contacts : Fragment() {

    private lateinit var contactsListView: ListView
    private val contactsList = ArrayList<String>()
    private val contactsMap = HashMap<String, String>()
    private val selectedContacts = mutableListOf<Pair<String, String>>()

    private val REQUEST_CODE_CONTACT = 1
    private val REQUEST_CODE_CALL = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        contactsListView = view.findViewById(R.id.contact_list)
        checkPermissions()

        contactsListView.setOnItemClickListener { _, _, position, _ ->
            val name = contactsList[position]
            val phoneNumber = contactsMap[name]
            makeCall(phoneNumber)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.statusBar)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACT)
        } else {
            loadContacts()
        }

        // Check CALL_PHONE permission separately
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CALL_PHONE), REQUEST_CODE_CALL)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts()
                }
            }
            REQUEST_CODE_CALL -> {
                // Handle CALL_PHONE permission result if needed
            }
        }
    }

    private fun loadContacts() {
        val contentResolver: ContentResolver = requireActivity().contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contactsList.add(name)
                contactsMap[name] = phoneNumber
            }
            cursor.close()
        } else {
            // Handle the case where no contacts are found
        }

        val showCheckBox = true
        val adapter = CustomAdapter(requireContext(), contactsList, showCheckBox, onContactCheckedChange = { contactName, isChecked ->
                val phoneNumber = contactsMap[contactName]
                // Ensure both values are of type String
                if (isChecked && phoneNumber != null) {
                    selectedContacts.add(contactName to phoneNumber) // Both contactName and phoneNumber are String
                } else {
                    selectedContacts.remove(
                        contactName to (phoneNumber ?: "")
                    ) // Use "" if phoneNumber is null
                }
            }, onContactClick = { contactName -> makeCall(contactsMap[contactName]) })
        contactsListView.adapter = adapter
    }

    private fun makeCall(phoneNumber: String?) {
        if (phoneNumber != null) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phoneNumber")
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CALL_PHONE), REQUEST_CODE_CALL)
            }
        }
    }
}
