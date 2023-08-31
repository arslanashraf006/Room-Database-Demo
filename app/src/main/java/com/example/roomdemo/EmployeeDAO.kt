package com.example.roomdemo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(employeeEntity: EmployeeEntity)

    @Update
     fun update(employeeEntity: EmployeeEntity)

    @Delete
     fun delete(employeeEntity: EmployeeEntity)

    @Query("SELECT * FROM `employee-table`")
    fun fetchAllEmployee() : Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM `employee-table` where id=:id")
    fun fetchEmployeeById(id:Int) : Flow<EmployeeEntity>

}