/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { CertificateService } from '../../../services/certificate.service';
import { DialogService } from 'app/services/dialog.service';
import { SnackbarService } from '../../../services/snackbar.service';
import internal from 'stream';
import { AppConfig } from 'config/app.config';

@Component({
  selector: 'app-certificates',
  templateUrl: './certificates.component.html',
  styleUrls: ['./certificates.component.scss']
})
export class DomainSettingsCertificatesComponent implements OnInit {
  private certificateTypes: any = {
    'javakeystore-am-certificate' : 'Java Keystore (.jks)',
    'pkcs12-am-certificate' : 'PKCS#12 (.p12)'
  };
  private certificateIcons: any = {
    'javakeystore-am-certificate' : 'security',
    'pkcs12-am-certificate' : 'security'
  };
  certificates: any[];
  domainId: string;
  threshold: number;

  constructor(private certificateService: CertificateService, private dialogService: DialogService,
              private snackbarService: SnackbarService, private route: ActivatedRoute, private dialog: MatDialog) { }

  ngOnInit() {
    this.domainId = this.route.snapshot.data['domain']?.id;
    this.certificates = this.route.snapshot.data['certificates'];
  }

  get isEmpty() {
    return !this.certificates || this.certificates.length === 0;
  }

  loadCertificates() {
    this.certificateService.findByDomain(this.domainId).subscribe(response => this.certificates = response);
  }

  publicKey(id, event) {
    event.preventDefault();
    this.certificateService.publicKeys(this.domainId, id).subscribe(response => {
      this.openPublicKeyInfo(response, false);
    }, error => {
      this.openPublicKeyInfo([], true);
    });
  }

  getCertificateTypeIcon(type) {
    if (this.certificateIcons[type]) {
      return this.certificateIcons[type];
    }
    return 'security';
  }

  displayType(type) {
    if (this.certificateTypes[type]) {
      return this.certificateTypes[type];
    }
    return type;
  }

  certificateWillExpire(cert) {
    return (cert.status == 'will_expire')
  }

  certificateIsExpired(cert) {
    return (cert.status == 'expired')
  }

  expireInDays(expiry) {
    return Math.ceil( (expiry - Date.now()) / (1000 * 3600 * 24));
  }

  private computeDate(days) {
    var result = new Date();
    result.setDate(result.getDate() + days);
    return result;
  }

  delete(id, event) {
    event.preventDefault();
    this.dialogService
      .confirm('Delete Certificate', 'Are you sure you want to delete this certificate ?')
      .subscribe(res => {
        if (res) {
          this.certificateService.delete(this.domainId, id).subscribe(response => {
            this.snackbarService.open('Certificate deleted');
            this.loadCertificates();
          });
        }
      });
  }

  openPublicKeyInfo(publicKeys, error) {
    const dialogRef = this.dialog.open(CertitificatePublicKeyDialog, { width : '700px' });
    dialogRef.componentInstance.title = 'Public certificate key';
    dialogRef.componentInstance.certificateKeys = publicKeys;
    dialogRef.componentInstance.error = error;
  }
}

@Component({
  selector: 'certificate-public-key-dialog',
  templateUrl: './dialog/public-key.component.html',
  styleUrls: ['./dialog/public-key.component.scss']
})
export class CertitificatePublicKeyDialog {
  public title: string;
  public certificateKeys: any[] = [];
  public error: boolean;

  constructor(public dialogRef: MatDialogRef<CertitificatePublicKeyDialog>, private snackbarService: SnackbarService) {}

  valueCopied(message) {
    this.snackbarService.open(message);
  }
}
