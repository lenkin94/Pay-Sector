package app.paysector.bill.repository;

import jakarta.validation.constraints.NotNull;
import app.paysector.bill.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findByBillNumber(@NotNull String billNumber);

    List<Bill> findByOwnerId(UUID id);
}
