package dev.visfox.forbiddenreforged.core.aspect;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Host-neutral description of a Forbidden Magic aspect.
 *
 * <p>Component identifiers are semantic references. A host adapter resolves them to the host's
 * native aspect objects and records any unavoidable mapping differences in its compatibility
 * report; the common model never imports a host's Aspect class.
 */
public record AspectDefinition(
    ContentId id,
    int color,
    List<ContentId> components,
    ContentId texture
) {
    public AspectDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(components, "components");
        Objects.requireNonNull(texture, "texture");
        if ((color & 0xFF000000) != 0) {
            throw new IllegalArgumentException("Aspect color must be RGB (0xRRGGBB)");
        }
        components = List.copyOf(components);
        if (!components.isEmpty() && components.size() != 2) {
            throw new IllegalArgumentException("An aspect must have zero or two components");
        }
        if (components.contains(id)) {
            throw new IllegalArgumentException("An aspect cannot be a component of itself");
        }
    }

    /** One positive quantity of a semantic aspect. */
    public record Amount(ContentId aspect, int amount) {
        public Amount {
            Objects.requireNonNull(aspect, "aspect");
            if (amount <= 0) {
                throw new IllegalArgumentException("Aspect amount must be positive");
            }
        }
    }

    /**
     * Immutable, insertion-ordered aspect cost. Duplicate identifiers are combined with overflow
     * checking, so host adapters receive one unambiguous amount per aspect.
     */
    public static final class Cost {
        public static final Cost EMPTY = new Cost(List.of());

        private final List<Amount> amounts;

        public Cost(List<Amount> amounts) {
            Objects.requireNonNull(amounts, "amounts");
            Map<ContentId, Integer> merged = new LinkedHashMap<>();
            for (Amount amount : amounts) {
                Objects.requireNonNull(amount, "amount");
                merged.merge(amount.aspect(), amount.amount(), Math::addExact);
            }
            this.amounts = merged.entrySet().stream()
                .map(entry -> new Amount(entry.getKey(), entry.getValue()))
                .toList();
        }

        public static Cost of(Amount... amounts) {
            return new Cost(List.of(amounts));
        }

        public List<Amount> amounts() {
            return amounts;
        }

        public int amountOf(ContentId aspect) {
            Objects.requireNonNull(aspect, "aspect");
            return amounts.stream()
                .filter(entry -> entry.aspect().equals(aspect))
                .mapToInt(Amount::amount)
                .findFirst()
                .orElse(0);
        }

        public int totalAmount() {
            return amounts.stream().mapToInt(Amount::amount).sum();
        }

        public Cost plus(Cost other) {
            Objects.requireNonNull(other, "other");
            List<Amount> combined = new ArrayList<>(amounts);
            combined.addAll(other.amounts);
            return new Cost(combined);
        }

        public boolean isEmpty() {
            return amounts.isEmpty();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Cost cost && amounts.equals(cost.amounts);
        }

        @Override
        public int hashCode() {
            return amounts.hashCode();
        }

        @Override
        public String toString() {
            return amounts.toString();
        }
    }
}
