package app.paysector.wallet.repository;

import app.paysector.wallet.model.Wallet;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Wallet findByOwnerId(UUID userId);

    Optional<Wallet> findByOwnerUsername(String receiverUsername);
}
