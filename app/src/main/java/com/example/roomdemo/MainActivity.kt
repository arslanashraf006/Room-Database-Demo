package com.example.roomdemo

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdemo.databinding.ActivityMainBinding
import com.example.roomdemo.databinding.DialogUpdateBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        var employeeDAO = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDAO)
        }
        lifecycleScope.launch {
            employeeDAO.fetchAllEmployee().collect{
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list,employeeDAO)
            }
        }
    }

    private fun addRecord(employeeDAO: EmployeeDAO){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty()){
           lifecycleScope.launch {
               employeeDAO.insert(EmployeeEntity(name = name, email = email))
               Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_SHORT).show()
               binding?.etName?.text?.clear()
               binding?.etEmailId?.text?.clear()
           }
        }else{
            Toast.makeText(applicationContext, "Name or Email cannot be blank", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setupListOfDataIntoRecyclerView(
        employeesList: ArrayList<EmployeeEntity>,
        employeeDAO: EmployeeDAO
    ){
        if(employeesList.isNotEmpty()){
            val itemAdapter = ItemAdapter(employeesList,
                {
                    updateId ->
                    updateRecordDialog(updateId,employeeDAO)
                },
                {
                   deleteId ->
                    lifecycleScope.launch {
                        employeeDAO.fetchEmployeeById(deleteId).collect {
                            if (it != null) {
                                deleteRecordAlertDialog(deleteId, employeeDAO, it)
                            }
                        }
                    }
                }
               )

            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        }else{
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id:Int, employeeDAO: EmployeeDAO){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDAO.fetchEmployeeById(id).collect{
                if(it != null) {
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmailId.setText(it.email)
                }
            }
        }
        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty()){
                lifecycleScope.launch {
                    employeeDAO.update(EmployeeEntity(id, name, email))
                    Toast.makeText(applicationContext, "Record Updated.",
                        Toast.LENGTH_SHORT)
                        .show()
                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(
                    applicationContext,
                    "Name or Email cannot be blank",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }
        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id:Int,employeeDao: EmployeeDAO,employee: EmployeeEntity) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Record")
        //set message for alert dialog
        builder.setMessage("Are you sure you wants to delete ${employee.name}.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(
                    applicationContext,
                    "Record deleted successfully.",
                    Toast.LENGTH_LONG
                ).show()

                dialogInterface.dismiss() // Dialog will be dismissed
            }

        }


        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
}