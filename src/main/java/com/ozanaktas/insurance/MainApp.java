package com.ozanaktas.insurance;

import com.ozanaktas.insurance.model.Admin;
import com.ozanaktas.insurance.model.Agent;
import com.ozanaktas.insurance.model.Customer;
import com.ozanaktas.insurance.model.InsuranceType;
import com.ozanaktas.insurance.model.Policy;
import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.repository.InMemoryPolicyRepository;
import com.ozanaktas.insurance.repository.InMemoryUserRepository;
import com.ozanaktas.insurance.repository.PolicyRepository;
import com.ozanaktas.insurance.repository.UserRepository;
import com.ozanaktas.insurance.service.AuthService;
import com.ozanaktas.insurance.service.ClaimQueueService;
import com.ozanaktas.insurance.service.PolicyService;
import com.ozanaktas.insurance.service.UndoService;
import com.ozanaktas.insurance.ui.ClaimsQueueView;
import com.ozanaktas.insurance.ui.CreateClaimView;
import com.ozanaktas.insurance.ui.CreatePolicyView;
import com.ozanaktas.insurance.ui.CustomerManagementView;
import com.ozanaktas.insurance.ui.DashboardView;
import com.ozanaktas.insurance.ui.LoginView;
import com.ozanaktas.insurance.ui.ManageEmployeesView;
import com.ozanaktas.insurance.ui.MyPoliciesView;
import com.ozanaktas.insurance.ui.ReportsView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;

public class MainApp extends Application {

    private Stage stage;

    private AuthService authService;
    private UndoService undoService;
    private ClaimQueueService claimQueueService;
    private PolicyService policyService;
    private UserRepository userRepository;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        // Repos
        this.userRepository = new InMemoryUserRepository();
        seedDemoUsers(this.userRepository);

        PolicyRepository policyRepo = new InMemoryPolicyRepository();
        this.policyService = new PolicyService(policyRepo);
        seedDemoPolicies();

        // Services
        this.authService = new AuthService(this.userRepository);
        this.undoService = new UndoService();
        this.claimQueueService = new ClaimQueueService(undoService);

        stage.setTitle("Insurance Policy Manager");
        showLogin();
        stage.show();
    }

    private void seedDemoUsers(UserRepository userRepo) {
        userRepo.save(new Admin("admin", "admin123", "Ozan Admin"));
        userRepo.save(new Agent("agent", "agent123", "Ozan Agent"));
        userRepo.save(new Customer("customer", "customer123", "Ozan Customer"));
    }

    private void seedDemoPolicies() {
        policyService.addPolicy(new Policy(
                "POL-1001",
                "customer",
                InsuranceType.TRAFFIC,
                1200,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusMonths(12)
        ));

        policyService.addPolicy(new Policy(
                "POL-2001",
                "customer",
                InsuranceType.HEALTH,
                3500,
                LocalDate.now().minusDays(40),
                LocalDate.now().plusMonths(10)
        ));
    }

    private void showLogin() {
       
        if (undoService != null) {
            undoService.clear();
        }

        var view = new LoginView(authService, this::showDashboard).getView();
        stage.setScene(new Scene(view, 520, 360));
    }

    private void showDashboard(User user) {
       
        Runnable onClaimsQueue = () -> showClaimsQueue(user);
        Runnable onCreateClaim = () -> showCreateClaim(user);
        Runnable onMyPolicies = () -> showMyPolicies(user);

       
        Runnable onManageEmployees = () -> showManageEmployees(user);
        Runnable onReports = () -> showReports(user);
        Runnable onCustomerManagement = () -> showCustomerManagement(user);
        Runnable onCreatePolicy = () -> showCreatePolicy(user);

        var view = new DashboardView(
                user,
                onManageEmployees,    
                onReports,           
                onCustomerManagement, 
                onCreatePolicy,      
                onClaimsQueue,       
                onMyPolicies,         
                onCreateClaim,        
                this::showLogin       
        ).getView();

        stage.setScene(new Scene(view, 640, 420));
    }

    private void showClaimsQueue(User user) {
        var view = new ClaimsQueueView(claimQueueService, undoService, () -> showDashboard(user)).getView();
        stage.setScene(new Scene(view, 820, 520));
    }

    private void showCreateClaim(User user) {
        var view = new CreateClaimView(user, claimQueueService, policyService, () -> showDashboard(user)).getView();
        stage.setScene(new Scene(view, 640, 520));
    }

    private void showMyPolicies(User user) {
        var view = new MyPoliciesView(user, policyService, undoService, () -> showDashboard(user)).getView();
        stage.setScene(new Scene(view, 820, 520));
    }

    private void showCreatePolicy(User user) {
        var view = new CreatePolicyView(user, policyService, undoService, () -> showDashboard(user)).getView();
        stage.setScene(new Scene(view, 760, 540));
    }

    private void showCustomerManagement(User user) {
        var view = new CustomerManagementView(user, userRepository, () -> showDashboard(user)).getView();
        stage.setScene(new Scene(view, 760, 600));
    }

    private void showReports(User user) {
        var view = new ReportsView(
                user,
                policyService,
                claimQueueService,
                undoService,
                userRepository,
                () -> showDashboard(user)
        ).getView();
        stage.setScene(new Scene(view, 760, 520));
    }

    private void showManageEmployees(User user) {
        var view = new ManageEmployeesView(user, userRepository, () -> showDashboard(user)).getView();
        stage.setScene(new Scene(view, 760, 620));
    }

    public static void main(String[] args) {
        launch(args);
    }
}