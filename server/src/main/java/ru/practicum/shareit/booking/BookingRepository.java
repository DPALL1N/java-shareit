package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            select b from Booking b
            where b.booker.id = :userId
              and (:state = 'ALL'
                or (:state = 'CURRENT'
                    and b.start <= :now and b.end >= :now)
                or (:state = 'PAST' and b.end < :now)
                or (:state = 'FUTURE' and b.start > :now)
                or (:state = 'WAITING'
                    and b.status = ru.practicum.shareit.booking.BookingStatus.WAITING)
                or (:state = 'REJECTED'
                    and b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED))
            order by b.start desc
            """)
    List<Booking> findByBookerAndState(@Param("userId") Long userId,
                                       @Param("state") String state,
                                       @Param("now") LocalDateTime now);

    @Query("""
            select b from Booking b
            where b.item.owner.id = :userId
              and (:state = 'ALL'
                or (:state = 'CURRENT'
                    and b.start <= :now and b.end >= :now)
                or (:state = 'PAST' and b.end < :now)
                or (:state = 'FUTURE' and b.start > :now)
                or (:state = 'WAITING'
                    and b.status = ru.practicum.shareit.booking.BookingStatus.WAITING)
                or (:state = 'REJECTED'
                    and b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED))
            order by b.start desc
            """)
    List<Booking> findByOwnerAndState(@Param("userId") Long userId,
                                      @Param("state") String state,
                                      @Param("now") LocalDateTime now);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId, Long bookerId,
                                                           BookingStatus status, LocalDateTime end);

    List<Booking> findByItemIdInAndStatusOrderByStartDesc(List<Long> itemIds, BookingStatus status);
}
