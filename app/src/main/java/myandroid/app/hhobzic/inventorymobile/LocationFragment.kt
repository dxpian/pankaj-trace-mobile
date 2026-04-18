package myandroid.app.hhobzic.inventorymobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentLocationBinding
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel

class LocationFragment : Fragment() {
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AppStatusSharedViewModel
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // this.viewPager.isUserInputEnabled = true

    }

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
