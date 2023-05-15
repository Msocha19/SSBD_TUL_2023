import { Address } from './address.model';

export interface RegisterAccountDto {
    email: string;
    login: string;
    password: string;
    firstName: string;
    lastName: string;
    language: string;
}

export interface RegisterOwnerDto extends RegisterAccountDto {
    address: Address;
}

export interface RegisterManagerDto extends RegisterAccountDto {
    address: Address;
    licenseNumber: string;
}