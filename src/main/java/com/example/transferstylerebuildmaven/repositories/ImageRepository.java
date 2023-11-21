package com.example.transferstylerebuildmaven.repositories;

import com.example.transferstylerebuildmaven.models.Image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query(value = """
    select i.imageName from StyleTransfer st left join Image i on
    st.id = i.id where st.isDeleted = false and concat(i.imageName, '.', LOWER(i.imageExtension) ) = :imageName
    """)
    Optional<Image> getImageByImageName(String imageName);
}
