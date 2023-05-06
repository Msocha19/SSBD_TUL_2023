import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { BehaviorSubject, catchError, map, of, tap } from 'rxjs';
import { LoginResponse } from '../model/login-response';
import jwtDecode from 'jwt-decode';
import { AccessLevel } from '../model/access-level';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RefreshSessionComponent } from '../components/modals/refresh-session/refresh-session.component';
import { ChooseAccessLevelComponent } from '../components/modals/choose-access-level/choose-access-level.component';
import { Router } from '@angular/router';
import { ToastService } from './toast.service';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authenticated = new BehaviorSubject<boolean>(false);
    private currentGroup = new BehaviorSubject<AccessLevel>(AccessLevel.NONE);

    constructor(
        private http: HttpClient,
        private modalService: NgbModal,
        private router: Router,
        private toastService: ToastService
    ) {
        this.handleLocalStorageContent();
        this.addLocalStorageListener();
    }

    handleLocalStorageContent() {
        const jwt = localStorage.getItem('jwt');
        if (jwt) {
            if (this.getDecodedJwtToken(jwt)) {
                if (!this.isJwtValid(jwt)) {
                    window.location.href = '/login';
                    this.clearUserData();
                }
                const group = localStorage.getItem('currentGroup');
                if (
                    group == AccessLevel.ADMIN ||
                    group == AccessLevel.MANAGER ||
                    group == AccessLevel.OWNER
                ) {
                    this.authenticated.next(true);
                    this.currentGroup.next(group as AccessLevel);
                    this.scheduleRefreshSessionPopUp();
                } else {
                    window.location.href = '/login';
                    this.clearUserData();
                }
            }
        }
    }

    addLocalStorageListener() {
        window.addEventListener(
            'storage',
            (event) => {
                if (event.storageArea == localStorage) {
                    const token = this.getJwt();
                    if (token == undefined) {
                        window.location.href = '/login';
                        this.clearUserData();
                    }
                }
            },
            false
        );
    }

    login(login: string, password: string) {
        return this.http
            .post<LoginResponse>(
                `${environment.apiUrl}/login`,
                { login, password },
                { observe: 'response' }
            )
            .pipe(
                map((response) => {
                    this.handleMultipleAccessLevels(response);
                    return true;
                }),
                catchError(() => of(false))
            );
    }

    handleMultipleAccessLevels(userData: HttpResponse<LoginResponse>) {
        const groups = this.getGroupsFromJwt(userData.body?.jwt);
        if (groups.length === 1) {
            this.loginSuccessfulHandler(userData, groups[0], true);
        } else {
            const modalRef = this.modalService.open(
                ChooseAccessLevelComponent,
                {
                    centered: true
                }
            );
            modalRef.componentInstance.groups = groups;
            modalRef.result
                .then((choice) => {
                    this.loginSuccessfulHandler(userData, choice, true);
                })
                .catch(() => {
                    this.loginSuccessfulHandler(userData, groups[0], true);
                });
        }
    }

    loginSuccessfulHandler(
        userData: HttpResponse<LoginResponse>,
        group: AccessLevel,
        redirectToDashboard: boolean
    ) {
        this.saveUserData(userData);
        this.setAuthenticated(true);
        this.setCurrentGroup(group);
        this.scheduleRefreshSessionPopUp();

        if (redirectToDashboard) {
            this.router.navigate(['/dashboard']);
        }
    }

    refreshToken() {
        this.http
            .post<LoginResponse>(
                `${environment.apiUrl}/refresh`,
                {
                    login: this.getLogin(),
                    refreshToken: this.getRefreshToken()
                },
                { observe: 'response' }
            )
            .pipe(
                tap((response) => {
                    this.loginSuccessfulHandler(
                        response,
                        this.getCurrentGroup(),
                        false
                    );
                }),
                catchError((err) => {
                    this.logout();
                    this.toastService.showDanger('Your session has expired.');
                    throw err;
                })
            )
            .subscribe();
    }

    scheduleRefreshSessionPopUp() {
        const decodedJwtToken = this.getDecodedJwtToken(this.getJwt());
        const millisBeforeJwtExpires = decodedJwtToken.exp * 1000 - Date.now();
        setTimeout(() => {
            this.modalService
                .open(RefreshSessionComponent)
                .result.then((refresh: boolean) => {
                    if (refresh) {
                        this.refreshToken();
                    } else {
                        if (!this.isSessionValid()) {
                            this.logout();
                            this.toastService.showDanger(
                                'Your session has expired.'
                            );
                        }
                    }
                })
                .catch(() => {
                    if (!this.isSessionValid()) {
                        this.logout();
                        this.toastService.showDanger(
                            'Your session has expired.'
                        );
                    }
                });
        }, millisBeforeJwtExpires * 0.9);
    }

    saveUserData(result: HttpResponse<LoginResponse>) {
        if (result.body) {
            const tokenInfo = this.getDecodedJwtToken(result.body.jwt);
            this.setLogin(tokenInfo.sub);
            this.setJwt(result.body.jwt);
            this.setRefreshToken(result.body.refreshToken);
        }
    }

    getGroups(): AccessLevel[] {
        const token = localStorage.getItem('jwt');
        if (token === null) {
            return [];
        }
        const tokenInfo = this.getDecodedJwtToken(token);
        return tokenInfo.groups;
    }

    getGroupsFromJwt(jwt: string | undefined): AccessLevel[] {
        if (jwt) {
            const tokenInfo = this.getDecodedJwtToken(jwt);
            return tokenInfo.groups;
        }
        return [];
    }

    getLogin(): string {
        return localStorage.getItem('login') ?? '';
    }

    getJwt(): string {
        return localStorage.getItem('jwt') ?? '';
    }

    getRefreshToken(): string {
        return localStorage.getItem('refreshToken') ?? '';
    }

    setLogin(login: string) {
        localStorage.setItem('login', login);
    }

    setJwt(jwt: string) {
        localStorage.setItem('jwt', jwt);
    }

    setRefreshToken(refreshToken: string) {
        localStorage.setItem('refreshToken', refreshToken);
    }

    getDecodedJwtToken(token: string): any {
        try {
            return jwtDecode(token);
        } catch (Error) {
            return null;
        }
    }

    isAuthenticated(): boolean {
        return this.authenticated.value;
    }

    hasAccessLevel(accessLevel: AccessLevel) {
        return this.getGroups().filter((al) => al == accessLevel).length > 0;
    }

    setAuthenticated(value: boolean) {
        this.authenticated.next(value);
    }

    getCurrentGroup(): AccessLevel {
        return this.currentGroup.value;
    }

    setCurrentGroup(group: AccessLevel) {
        this.currentGroup.next(group);
        localStorage.setItem('currentGroup', group);
    }

    clearUserData() {
        localStorage.clear();
    }

    logout() {
        this.clearUserData();
        this.setAuthenticated(false);
        this.router.navigate(['/login']);
    }

    isSessionValid(): boolean {
        const decodedJwtToken = this.getDecodedJwtToken(this.getJwt());
        if (decodedJwtToken) {
            return decodedJwtToken.exp * 1000 > Date.now();
        }
        return false;
    }

    isJwtValid(jwt: string): boolean {
        const decodedJwtToken = this.getDecodedJwtToken(jwt);
        if (decodedJwtToken) {
            return decodedJwtToken.exp * 1000 > Date.now();
        }
        return false;
    }
}
