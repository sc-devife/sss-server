package com.sss.app.entity.organizations;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.util.CompareUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organizations {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_seq_gen")
    @SequenceGenerator(
            name = "org_seq_gen",
            sequenceName = "organizations_seqp_seq",
            allocationSize = 1
    )
    @Column(name = "seqp")
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column (name = "registered_name")
    private String registeredName;

    @Column (name = "display_name")
    private String displayName;

    @Column (name = "support_ph_num")
    private String supportPhNum;

    public static Organizations create(OrganizationsDto dto) {

        OrganizationsBuilder builder = Organizations.builder();
        builder.registeredName(dto.getRegistered_name());
        builder.displayName(dto.getDisplay_name());
        builder.supportPhNum(dto.getSupport_ph_num());

        return builder.build();
    }

    public void update(OrganizationsDto dto) {

        if (dto.getRegistered_name() != null && CompareUtil.hasChanged(dto.getRegistered_name(), this.registeredName)) {
            this.registeredName = dto.getRegistered_name();
        }

        if (CompareUtil.hasChanged(dto.getDisplay_name(), this.displayName)) {
            this.displayName = dto.getDisplay_name();
        }
        System.out.println("dto.getSupport_ph_num() == " + dto.getSupport_ph_num());
        System.out.println("this ph number == " + this.getSupportPhNum());
        if (CompareUtil.hasChanged(dto.getSupport_ph_num(), this.getSupportPhNum())) {
            System.out.println("Setting ph number == " + this.getSupportPhNum());
            this.supportPhNum = dto.getSupport_ph_num();
        }
    }
}
