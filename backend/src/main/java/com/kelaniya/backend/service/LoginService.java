package com.kelaniya.backend.service;

import com.kelaniya.backend.entity.request.JwtRequest;
import com.kelaniya.backend.entity.response.JwtResponse;
import com.kelaniya.backend.entity.Users;
import com.kelaniya.backend.repository.UserRepository;
import com.kelaniya.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class LoginService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private AuthenticationManager authenticationManager;

  public JwtResponse createJwtToken(JwtRequest jwtRequest) throws Exception{
    String username = jwtRequest.getUsername();
    String password = jwtRequest.getPassword();
    authenticate(username, password);

    UserDetails userDetails = loadUserByUsername(username);
    String newToken = jwtUtil.generateToken(userDetails);
    Users users = userRepository.findById(username).get();
    return new JwtResponse(users, newToken);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Users users = userRepository.findById(username).get();

    if(users != null){
      return new org.springframework.security.core.userdetails.User(
        users.getUsername(),
        users.getPassword(),
        getAuthority(users)
      );
    } else {
      throw new UsernameNotFoundException("Username is not found with username: "+ username);
    }
  }

  private Set getAuthority(Users users) {
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    users.getRole().forEach(role -> {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
    });
    return authorities;
  }

  private void authenticate(String username, String password) throws Exception{
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

    } catch (DisabledException e){
      throw new Exception("User is disabled");
    } catch (BadCredentialsException e){
      throw new Exception("Bad credentials");
    }
  }
}
