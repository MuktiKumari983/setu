package com.sih.innovationplatform.config;

import com.sih.innovationplatform.university.Faculty;
import com.sih.innovationplatform.university.FacultyRepository;
import com.sih.innovationplatform.university.University;
import com.sih.innovationplatform.university.UniversityRepository;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederDeterminismTest {

    @Mock private FacultyRepository facultyRepo;
    @Mock private UniversityRepository universityRepo;
    @Mock private UserRepository userRepo;
    @InjectMocks private DataSeeder seeder;

    @Test
    void seedScoresAreStableAndBounded() {
        assertEquals(0.90, DataSeeder.seedScore(5, "UNIVERSITY"), 0.000001);
        assertEquals(0.92, DataSeeder.seedScore(1, "TEAM"), 0.000001);
        assertEquals(0.88, DataSeeder.seedScore(0, "MENTOR"), 0.000001);
        assertEquals(0.87, DataSeeder.seedScore(0, "INDUSTRY"), 0.000001);
        assertEquals(DataSeeder.seedScore(3, "UNIVERSITY"), DataSeeder.seedScore(3, "UNIVERSITY"), 0.000001);
        assertTrue(DataSeeder.seedScore(99, "UNIVERSITY") <= 0.99);
    }

    @Test
    void facultyUserIsLinkedToTheMentorUniversityRatherThanTheFacultyId() {
        University university = university(7L);
        Faculty mentor = mentor(42L, university);
        when(facultyRepo.findAll()).thenReturn(List.of(mentor));
        when(universityRepo.findById(7L)).thenReturn(Optional.of(university));
        User facultyUser = new User();

        seeder.linkFacultyUserToUniversity(facultyUser);

        assertEquals(7L, facultyUser.getLinkedUniversityId());
        verify(userRepo).save(facultyUser);
    }

    @Test
    void facultyUserIsNotLinkedWhenUniversityDoesNotExist() {
        University university = university(99L);
        when(facultyRepo.findAll()).thenReturn(List.of(mentor(42L, university)));
        when(universityRepo.findById(99L)).thenReturn(Optional.empty());
        User facultyUser = new User();

        assertThrows(IllegalStateException.class,
                () -> seeder.linkFacultyUserToUniversity(facultyUser));

        assertNull(facultyUser.getLinkedUniversityId());
        verifyNoInteractions(userRepo);
    }

    private static University university(Long id) {
        University university = new University("Indian Institute of Technology Delhi",
                "New Delhi", "demo");
        university.setId(id);
        return university;
    }

    private static Faculty mentor(Long id, University university) {
        Faculty mentor = new Faculty(university, null, "Dr. Anjali Verma", "Professor",
                "Waste Management", "Smart waste systems", "Smart Bin pilot", "mentor@example.com");
        mentor.setId(id);
        return mentor;
    }
}
