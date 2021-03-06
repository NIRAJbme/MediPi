/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.clinical.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
/**
  * Entity Class to manage DB access for recording device data
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "recording_device_data")
@NamedQueries({
    //Added
    @NamedQuery(name = "RecordingDeviceData.isAlreadyStored", query = "SELECT d FROM RecordingDeviceData d WHERE d.attributeId = :attributeId AND d.dataValue = :dataValue AND d.dataValueTime = :dataValueTime AND d.patientUuid = :patientUuid"),
    @NamedQuery(name = "RecordingDeviceData.findByPatientUuidAfterDate", query = "SELECT d FROM RecordingDeviceData d WHERE d.patientUuid = :patientUuid AND d.dataValueTime>:requestDate"),
    @NamedQuery(name = "RecordingDeviceData.dateOfLatestPatientGroupSync", query = "SELECT MAX(d.downloadedTime) FROM RecordingDeviceData d, RecordingDeviceAttribute a, RecordingDeviceType t, Patient p WHERE d.attributeId = a.attributeId AND a.typeId = t.typeId AND d.patientUuid = p.patientUuid AND p.patientGroupUuid = :patientGroup"),
    @NamedQuery(name = "RecordingDeviceData.dateOfLatestMeasurement", query = "SELECT MAX(d.dataValueTime) FROM RecordingDeviceData d, RecordingDeviceAttribute a, RecordingDeviceType t WHERE d.attributeId = a.attributeId AND a.typeId = t.typeId AND d.patientUuid = :patientUuid AND t.type = :type"),
    @NamedQuery(name = "RecordingDeviceData.findByPatientAndScheduledTime", query = "SELECT d FROM RecordingDeviceData d WHERE d.patientUuid = :patientUuid AND d.scheduleEffectiveTime >= :scheduleEffectiveTime AND d.scheduleExpiryTime <= :scheduleExpiryTime AND d.dataValueTime IN (SELECT MAX(e.dataValueTime) FROM RecordingDeviceData e WHERE e.patientUuid = :patientUuid AND e.scheduleEffectiveTime >= :scheduleEffectiveTime AND e.scheduleExpiryTime <= :scheduleExpiryTime)"),
    @NamedQuery(name = "RecordingDeviceData.findByPatientAndAttributeAndPeriod", query = "SELECT d FROM RecordingDeviceData d WHERE d.patientUuid.patientUuid = :patientUuid AND d.attributeId.attributeId = :attributeId AND d.dataValueTime >= :periodStartTime AND d.dataValueTime < :periodEndTime"),

    @NamedQuery(name = "RecordingDeviceData.findAll", query = "SELECT r FROM RecordingDeviceData r"),
    @NamedQuery(name = "RecordingDeviceData.findByDataId", query = "SELECT r FROM RecordingDeviceData r WHERE r.dataId = :dataId"),
    @NamedQuery(name = "RecordingDeviceData.findByDataValue", query = "SELECT r FROM RecordingDeviceData r WHERE r.dataValue = :dataValue"),
    @NamedQuery(name = "RecordingDeviceData.findByDataValueTime", query = "SELECT r FROM RecordingDeviceData r WHERE r.dataValueTime = :dataValueTime"),
    @NamedQuery(name = "RecordingDeviceData.findByDownloadedTime", query = "SELECT r FROM RecordingDeviceData r WHERE r.downloadedTime = :downloadedTime")})
public class RecordingDeviceData implements Serializable {



    @Size(max = 2147483647)
    @Column(name = "alert_status")
    private String alertStatus;
    @Column(name = "schedule_effective_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduleEffectiveTime;
    @Column(name = "schedule_expiry_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduleExpiryTime;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "data_id")
    private Long dataId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "data_value")
    private String dataValue;
    @Basic(optional = false)
    @NotNull
    @Column(name = "data_value_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataValueTime;
    @Basic(optional = false)
    @NotNull
    @Column(name = "downloaded_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date downloadedTime;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataId")
    private Collection<Alert> alertCollection;
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid")
    @ManyToOne(optional = false)
    private Patient patientUuid;
    @JoinColumn(name = "attribute_id", referencedColumnName = "attribute_id")
    @ManyToOne(optional = false)
    private RecordingDeviceAttribute attributeId;

    public RecordingDeviceData() {
    }

    public RecordingDeviceData(Long dataId) {
        this.dataId = dataId;
    }

    public RecordingDeviceData(Long dataId, String dataValue, Date dataValueTime, Date downloadedTime) {
        this.dataId = dataId;
        this.dataValue = dataValue;
        this.dataValueTime = dataValueTime;
        this.downloadedTime = downloadedTime;
    }

    public Long getDataId() {
        return dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public Date getDataValueTime() {
        return dataValueTime;
    }

    public void setDataValueTime(Date dataValueTime) {
        this.dataValueTime = dataValueTime;
    }

    public Date getDownloadedTime() {
        return downloadedTime;
    }

    public void setDownloadedTime(Date downloadedTime) {
        this.downloadedTime = downloadedTime;
    }

    public Collection<Alert> getAlertCollection() {
        return alertCollection;
    }

    public void setAlertCollection(Collection<Alert> alertCollection) {
        this.alertCollection = alertCollection;
    }

    public Patient getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(Patient patientUuid) {
        this.patientUuid = patientUuid;
    }

    public RecordingDeviceAttribute getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(RecordingDeviceAttribute attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dataId != null ? dataId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordingDeviceData)) {
            return false;
        }
        RecordingDeviceData other = (RecordingDeviceData) object;
        if ((this.dataId == null && other.dataId != null) || (this.dataId != null && !this.dataId.equals(other.dataId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.entities.RecordingDeviceData[ dataId=" + dataId + " ]";
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    public Date getScheduleEffectiveTime() {
        return scheduleEffectiveTime;
    }

    public void setScheduleEffectiveTime(Date scheduleEffectiveTime) {
        this.scheduleEffectiveTime = scheduleEffectiveTime;
    }

    public Date getScheduleExpiryTime() {
        return scheduleExpiryTime;
    }

    public void setScheduleExpiryTime(Date scheduleExpiryTime) {
        this.scheduleExpiryTime = scheduleExpiryTime;
    }


}
