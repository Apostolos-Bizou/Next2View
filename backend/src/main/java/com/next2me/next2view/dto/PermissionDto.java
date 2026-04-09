package com.next2me.next2view.dto;

public record PermissionDto(
    boolean viewFinance,
    boolean viewLegal,
    boolean viewDev,
    boolean viewMarketing,
    boolean viewFinancials,
    boolean viewCeoNotes,
    boolean updateTasks,
    boolean uploadFiles,
    boolean createProject,
    boolean editProject,
    boolean manageUsers,
    boolean manageCompanies,
    boolean aiCeoReport,
    boolean aiContract
) {}