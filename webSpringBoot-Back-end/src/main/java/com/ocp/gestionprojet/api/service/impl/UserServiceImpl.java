package com.ocp.gestionprojet.api.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocp.gestionprojet.api.exception.EntityNotFoundException;
import com.ocp.gestionprojet.api.exception.UserAlreadyExistsException;
import com.ocp.gestionprojet.api.mapper.PersonnelMapper;
import com.ocp.gestionprojet.api.model.dto.authDto.AuthResponseDto;
import com.ocp.gestionprojet.api.model.dto.authDto.LoginDto;
import com.ocp.gestionprojet.api.model.dto.authDto.RegisterAdminDto;
import com.ocp.gestionprojet.api.model.dto.authDto.RegisterManagerDto;
import com.ocp.gestionprojet.api.model.dto.authDto.RegisterMemberDto;
import com.ocp.gestionprojet.api.model.dto.personDto.PersonDto;
import com.ocp.gestionprojet.api.model.entity.AdminEntity;
import com.ocp.gestionprojet.api.model.entity.ManagerEntity;
import com.ocp.gestionprojet.api.model.entity.MemberEntity;
import com.ocp.gestionprojet.api.model.entity.UserEntity;
import com.ocp.gestionprojet.api.repository.UserRepository;
import com.ocp.gestionprojet.api.service.interfaces.AdminService;
import com.ocp.gestionprojet.api.service.interfaces.EmailService;
import com.ocp.gestionprojet.api.service.interfaces.ManagerService;
import com.ocp.gestionprojet.api.service.interfaces.MemberService;
import com.ocp.gestionprojet.api.service.interfaces.UserService;
import com.ocp.gestionprojet.security.JWTGenerator;
import com.ocp.gestionprojet.shared.RolesUser;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTGenerator jwtGenerator;
    @Autowired
    private PersonnelMapper personnelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ManagerService managerService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AdminService adminService;
     @Autowired
    private EmailService emailService;

    // Authenticate a user and generate an authentication token
    @Override
    @Transactional
    public AuthResponseDto authenticateUser(LoginDto loginDto) {
        try {
            // Perform authentication
            Authentication authentication = authenticate(loginDto);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Retrieve the user and generate a JWT token
            UserEntity user = getUserByEmail(authentication.getName());
            String token = jwtGenerator.generateToken(authentication);
            PersonDto personDto = getPersonDto(user);

            // Build and return the authentication response
            return buildAuthResponse(token, user, personDto);
        } catch (UsernameNotFoundException e) {
            throw new AuthenticationException("User not found") {};
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed") {};
        }
    }

    // Authenticate a user using email and password
    @Override
    public Authentication authenticate(LoginDto loginDto) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()));
    }

    // Find a user by their email
    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Convert a UserEntity to a PersonDto based on the user's role
    @Override
    public PersonDto getPersonDto(UserEntity user) {
        if (user.getMember() != null) {
            return personnelMapper.toDto(user.getMember());
        }
        if (user.getManager() != null) {
            return personnelMapper.toDto(user.getManager());
        }
        if (user.getAdmin() != null) {
            return personnelMapper.toDto(user.getAdmin());
        }
        throw new IllegalStateException("User has no associated role");
    }

    // Build an authentication response with token, user details, and person data
    @Override
    public AuthResponseDto buildAuthResponse(String token, UserEntity user, PersonDto personDto) {
        return new AuthResponseDto(
                token,
                user.getId(),
                user.getRole(),
                personDto);
    }

    // Register a new manager (chef) and associate them with a team
    @Override
    @Transactional
    public void registerManager(RegisterManagerDto registerDto)
            throws UserAlreadyExistsException, EntityNotFoundException {
        if (userRepository.existsByEmail(registerDto.getManager().getEmail())) {
            throw new UserAlreadyExistsException("Username is taken!");
        }

        String generatedPassword = generateRandomPassword();
        UserEntity user = createUserEntityToManager(registerDto, generatedPassword);
        userRepository.save(user);

        ManagerEntity savedChefEntity = managerService.addManagersToTeams(
                registerDto.getManager(), user);
        user.setManager(savedChefEntity);
        userRepository.save(user);

        sendPasswordByEmail(user.getEmail(), generatedPassword);
    }
    

    // Create a UserEntity for a manager (chef) with the provided data
    @Override
    public UserEntity createUserEntityToManager(RegisterManagerDto registerDto, String password) {
        UserEntity user = new UserEntity();
        user.setEmail(registerDto.getManager().getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(RolesUser.CHEF);
        return user;
    }
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void sendPasswordByEmail(String email, String password) {
        String subject = "Votre nouveau mot de passe";
        String message = "Voici votre nouveau mot de passe: " + password + 
                         "\nVeuillez le changer après votre première connexion.";
        emailService.sendSimpleMessage(email, subject, message);
    }

    // Register a new employee and associate them with a team
    @Override
    @Transactional
    public void registerMember(RegisterMemberDto registerDto)
            throws UserAlreadyExistsException, EntityNotFoundException {
        if (userRepository.existsByEmail(registerDto.getMember().getEmail())) {
            throw new UserAlreadyExistsException("Username is taken!");
        }
        String generatedPassword = generateRandomPassword();

        UserEntity user = createUserEntityToMember(registerDto,generatedPassword);
        userRepository.save(user);
        MemberEntity savedEmployeEntity = memberService.addMemberToTeam(registerDto.getMember(), user);
        user.setMember(savedEmployeEntity);

        userRepository.save(user);
        sendPasswordByEmail(user.getEmail(), generatedPassword);
    }

    // Create a UserEntity for an employee with the provided data
    @Override
    public UserEntity createUserEntityToMember(RegisterMemberDto registerDto,String password) {
        UserEntity user = new UserEntity();
        user.setEmail(registerDto.getMember().getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(RolesUser.USER);
        return user;
    }

    // Register a new admin and associate them with the appropriate role
    @Override
    @Transactional
    public void registerAdmin(RegisterAdminDto registerDto) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(registerDto.getPerson().getEmail())) {
            throw new UserAlreadyExistsException("Username is taken!");
        }

        UserEntity user = createUserEntityToAdmin(registerDto);
        userRepository.save(user);
        AdminEntity savedAdmin = adminService.save(registerDto.getPerson(), user);
        savedAdmin.setUser(user);

        user.setAdmin(savedAdmin);
        userRepository.save(user);
    }

    // Create a UserEntity for an admin with the provided data
    @Override
    public UserEntity createUserEntityToAdmin(RegisterAdminDto registerDto) {
        UserEntity user = new UserEntity();
        user.setEmail(registerDto.getPerson().getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole(RolesUser.ADMIN);
        return user;
    }

    @Transactional
    public void updatePassword(String username, String newPassword) {
        UserEntity user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);

        user.setPasswordUpdated(true);

        userRepository.save(user);
    }

    @Override
    public int registerMembersFromCsv(List<RegisterMemberDto> members) {
        int successCount = 0;
        for (RegisterMemberDto registerDto : members) {
            try {
                registerMember(registerDto);
                successCount++;
            } catch (Exception e) {
                // Log the error and continue with the next member
                System.err.println("Error registering member: " + e.getMessage());
            }
        }
        return successCount;
    }

}
