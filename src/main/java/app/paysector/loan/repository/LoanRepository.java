package app.paysector.loan.repository;

import app.paysector.loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findAllByOwnerIdOrderByStartPeriodDesc(UUID userId);
}
