package com.example.ledger.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ledger.viewmodel.AppViewModelFactory
import com.example.ledger.viewmodel.BackupViewModel
import com.example.ledger.viewmodel.CustomerDetailViewModel
import com.example.ledger.viewmodel.EntryViewModel
import com.example.ledger.viewmodel.HomeViewModel
import com.example.ledger.ui.screens.*

@Composable
fun AppNavHost(appViewModelFactory: AppViewModelFactory) {
    val navController = rememberNavController()
    val context = LocalContext.current
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val vm: HomeViewModel = viewModel(factory = appViewModelFactory)
            HomeScreen(
                viewModel = vm,
                onAdd = { navController.navigate("addCustomer") },
                onOpenDetails = { navController.navigate("details/$it") },
                onOpenBackup = { navController.navigate("backup") }
            )
        }
        composable("addCustomer") {
            val vm: HomeViewModel = viewModel(factory = appViewModelFactory)
            AddCustomerScreen(onSave = { n, p -> vm.addCustomer(n, p); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("details/{customerId}", arguments = listOf(navArgument("customerId") { type = NavType.LongType })) { backStack ->
            val id = backStack.arguments?.getLong("customerId") ?: 0L
            val vm: CustomerDetailViewModel = viewModel(factory = appViewModelFactory)
            CustomerDetailsScreen(
                customerId = id,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onAddItem = { navController.navigate("addItem/$id") },
                onAddPayment = { navController.navigate("addPayment/$id") },
                onSharePdf = { uri ->
                    context.startActivity(Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    })
                }
            )
        }
        composable("addItem/{customerId}", arguments = listOf(navArgument("customerId") { type = NavType.LongType })) { backStack ->
            val id = backStack.arguments?.getLong("customerId") ?: 0L
            val vm: EntryViewModel = viewModel(factory = appViewModelFactory)
            AddItemScreen(customerId = id, onSave = { d, t, q, p -> vm.addItem(id, d, t, q, p); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("addPayment/{customerId}", arguments = listOf(navArgument("customerId") { type = NavType.LongType })) { backStack ->
            val id = backStack.arguments?.getLong("customerId") ?: 0L
            val vm: EntryViewModel = viewModel(factory = appViewModelFactory)
            AddPaymentScreen(customerId = id, onSave = { d, a -> vm.addPayment(id, d, a); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("backup") {
            val vm: BackupViewModel = viewModel(factory = appViewModelFactory)
            BackupScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
