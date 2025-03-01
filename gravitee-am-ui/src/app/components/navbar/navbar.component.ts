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
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {DomainService} from "../../services/domain.service";
import {Subscription} from "rxjs";
import {NavbarService} from "./navbar.service";
import {SnackbarService} from "../../services/snackbar.service";
import * as _ from 'lodash';
import {SidenavService} from "../sidenav/sidenav.service";
import {EnvironmentService} from "../../services/environment.service";
import {MatSelectChange} from "@angular/material/select";
import { UserNotificationsService } from 'app/services/user-notifications.service';

@Component({
  selector: 'gv-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, OnDestroy {
  private domainSubscription: Subscription;
  private environmentSubscription: Subscription;
  private sidenavSubscription: Subscription;
  reducedMode = false;
  domains: any[];
  currentDomain: any = {};
  navLinks: any[];
  currentEnvironment;
  notifications: any[];
  // notification refresh interval in millis
  readonly REFRESH_INTERVAL = 10000;

  constructor(private authService: AuthService,
              private domainService: DomainService,
              private navbarService: NavbarService,
              private snackbarService: SnackbarService,
              private sidenavService: SidenavService,
              private environmentService: EnvironmentService,
              public router: Router,
              private userNotificationsService: UserNotificationsService) {
  }

  ngOnInit() {
    this.initNavLinks();

    this.environmentSubscription = this.environmentService.currentEnvironmentObs$.subscribe(environment => {
      this.currentEnvironment = environment;
      this.initNavLinks();
    });
    this.domainSubscription = this.navbarService.currentDomainObs$.subscribe(data => {
      this.currentDomain = data;
    });
    this.sidenavSubscription = this.sidenavService.resizeSidenavObservable.subscribe(reducedMode => this.reducedMode = reducedMode);
    
    // read notifications on component initialization and then trigger a refresh in regular period
    this.userNotificationsService.listNotifications().subscribe(data => {
      this.notifications = data;

      setInterval(() => {
        this.userNotificationsService.listNotifications().subscribe(data => {
          this.notifications = data;
        })
      }, this.REFRESH_INTERVAL)
    })
  }

  ngOnDestroy(): void {
    this.environmentSubscription.unsubscribe();
    this.domainSubscription.unsubscribe();
    this.sidenavSubscription.unsubscribe();
  }

  get user() {
    return this.authService.user() != null ? this.authService.user() : null;
  }

  listDomains() {
    if(this.hasCurrentEnvironment()) {
      this.domainService.findByEnvironment(0,5).subscribe(response => this.domains = response.data);
    } else {
      this.domains = [];
    }
  }

  displayBreadcrumb(): boolean {
    return !this.router.url.startsWith('/domains/new') &&
      !this.router.url.startsWith('/login') &&
      !this.router.url.startsWith('/logout') &&
      !this.router.url.startsWith('/404');
  }

  private initNavLinks() {

    this.navLinks = [];

    if (this.hasCurrentEnvironment() && this.canDisplay(['domain_list'])) {
      this.navLinks.push({'href': '/environments/' + this.currentEnvironment.hrids[0] + '/domains', 'label': 'All domains', 'icon': 'developer_board'});
    }

    if (this.hasCurrentEnvironment() && this.canDisplay(['domain_create'])) {
      this.navLinks.push({'href': '/environments/' + this.currentEnvironment.hrids[0] + '/domains/new', 'label': 'Create domain', 'icon': 'add'});
    }

    this.navLinks.push({'href': '/logout', 'label': 'Sign out', 'icon': 'exit_to_app'});
  }

  hasCurrentEnvironment(): boolean {
    return this.currentEnvironment && this.currentEnvironment !== EnvironmentService.NO_ENVIRONMENT;
  }

  private canDisplay(permissions): boolean {
    return this.authService.hasPermissions(permissions);
  }

  hasNotifications() {
    return this.notifications && this.notifications.length > 0;
  }

  markNotificationAsRead(notificationId, event) {
    if (this.notifications.filter(notif => notif.id !== notificationId).length > 0) {
      // keep menu open only of there are more notifications
      event.stopPropagation();
    }
    this.userNotificationsService.markAsRead(notificationId).subscribe(data => {
      this.notifications = this.notifications.filter(notif => notif.id !== notificationId);
    });
  }
}
