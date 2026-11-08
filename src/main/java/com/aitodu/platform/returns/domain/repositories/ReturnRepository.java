package com.aitodu.platform.returns.domain.repositories;

import com.aitodu.platform.returns.domain.model.aggregates.CustomerReturn;
import java.util.*;

public interface ReturnRepository {
  void save(CustomerReturn record);

  List<CustomerReturn> findAll(UUID tenant);
}
